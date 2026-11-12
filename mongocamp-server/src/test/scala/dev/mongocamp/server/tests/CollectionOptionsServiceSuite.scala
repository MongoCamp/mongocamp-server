package dev.mongocamp.server.tests

import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.CollectionCollation
import dev.mongocamp.server.model.CollectionOptions
import dev.mongocamp.server.service.CollectionOptionsService
import org.bson.BsonDocument

/** Needs no server and no database: the rules and the conversion of the options of a collection. */
class CollectionOptionsServiceSuite extends munit.FunSuite {

  private def refused(options: CollectionOptions, forModify: Boolean = false): String = {
    val error =
      intercept[MongoCampException](if (forModify) CollectionOptionsService.checkForModify(options) else CollectionOptionsService.checkForCreate(options))
    error.getMessage
  }

  test("no options and valid options are accepted for the creation") {
    CollectionOptionsService.checkForCreate(CollectionOptions())
    CollectionOptionsService.checkForCreate(CollectionOptions(capped = Some(true), sizeInBytes = Some(4096), maxDocuments = Some(5)))
    CollectionOptionsService.checkForCreate(CollectionOptions(validationLevel = Some("moderate"), validationAction = Some("warn")))
    CollectionOptionsService.checkForCreate(CollectionOptions(collation = Some(CollectionCollation("en", Some(2)))))
  }

  test("a capped collection needs a size, the size is only for a capped collection") {
    assert(refused(CollectionOptions(capped = Some(true))).contains("sizeInBytes"))
    assert(refused(CollectionOptions(capped = Some(true), sizeInBytes = Some(0))).contains("sizeInBytes"))
    assert(refused(CollectionOptions(sizeInBytes = Some(4096))).contains("capped"))
    assert(refused(CollectionOptions(maxDocuments = Some(5))).contains("capped"))
    assert(refused(CollectionOptions(capped = Some(true), sizeInBytes = Some(4096), maxDocuments = Some(0))).contains("maxDocuments"))
  }

  test("the level, the action and the collation are checked") {
    assert(refused(CollectionOptions(validationLevel = Some("hard"))).contains("validationLevel"))
    assert(refused(CollectionOptions(validationAction = Some("ignore"))).contains("validationAction"))
    assert(refused(CollectionOptions(collation = Some(CollectionCollation(" ")))).contains("locale"))
    assert(refused(CollectionOptions(collation = Some(CollectionCollation("en", Some(6))))).contains("strength"))
  }

  test("only the validation can be changed") {
    CollectionOptionsService.checkForModify(CollectionOptions(validationLevel = Some("off")))
    CollectionOptionsService.checkForModify(CollectionOptions(validator = Some(Map("a" -> 1))))
    assert(refused(CollectionOptions(capped = Some(true)), forModify = true).contains("cannot be changed"))
    assert(refused(CollectionOptions(collation = Some(CollectionCollation("en"))), forModify = true).contains("cannot be changed"))
    assert(refused(CollectionOptions(), forModify = true).contains("nothing to change"))
    assert(refused(CollectionOptions(validationLevel = Some("hard")), forModify = true).contains("validationLevel"))
  }

  test("the options of the driver") {
    val result = CollectionOptionsService.toCreateCollectionOptions(
      CollectionOptions(
        capped = Some(true),
        sizeInBytes = Some(4096),
        maxDocuments = Some(5),
        validator = Some(Map("$jsonSchema" -> Map("required" -> List("name")))),
        validationLevel = Some("moderate"),
        validationAction = Some("warn"),
        collation = Some(CollectionCollation("en", Some(2), Some(true), Some(true)))
      )
    )
    assertEquals(result.isCapped, true)
    assertEquals(result.getSizeInBytes, 4096L)
    assertEquals(result.getMaxDocuments, 5L)
    assertEquals(result.getValidationOptions.getValidationLevel.getValue, "moderate")
    assertEquals(result.getValidationOptions.getValidationAction.getValue, "warn")
    assertEquals(result.getCollation.getLocale, "en")
    assertEquals(result.getCollation.getNumericOrdering, java.lang.Boolean.TRUE)
    assert(result.getValidationOptions.getValidator != null)
  }

  test("a size and a maximum are not given to a collection that is not capped") {
    val result = CollectionOptionsService.toCreateCollectionOptions(CollectionOptions())
    assertEquals(result.isCapped, false)
    // the driver always has validation options, empty ones: the database gets no validator, level and action
    assert(result.getValidationOptions.getValidator == null)
    assert(result.getValidationOptions.getValidationLevel == null)
    assert(result.getValidationOptions.getValidationAction == null)
  }

  test("the command that changes the validation") {
    val command = CollectionOptionsService.collModCommand("people", CollectionOptions(validator = Some(Map("a" -> 1)), validationLevel = Some("off")))
    assertEquals(command.get("collMod").map(_.asString().getValue), Some("people"))
    assertEquals(command.get("validationLevel").map(_.asString().getValue), Some("off"))
    assertEquals(command.get("validationAction"), None)
    assert(command.get("validator").exists(_.isDocument))
  }

  test("the options of an entry of listCollections") {
    val entry = BsonDocument.parse(
      """{"name": "people", "type": "collection", "options": {"capped": true, "size": 4096, "max": 5, "validator": {"$jsonSchema": {"required": ["name"]}}, "validationLevel": "strict", "validationAction": "error", "collation": {"locale": "en", "strength": 2}}}"""
    )
    val options = CollectionOptionsService.parseOptions(entry)
    assertEquals(options.capped, Some(true))
    assertEquals(options.sizeInBytes, Some(4096L))
    assertEquals(options.maxDocuments, Some(5L))
    assertEquals(options.validationLevel, Some("strict"))
    assertEquals(options.validationAction, Some("error"))
    assertEquals(options.collation.map(_.locale), Some("en"))
    assertEquals(options.collation.flatMap(_.strength), Some(2))
    assert(options.validator.exists(_.contains("$jsonSchema")), options.validator.toString)
  }

  test("a collection without options is not capped and has no validator") {
    val options = CollectionOptionsService.parseOptions(BsonDocument.parse("""{"name": "plain", "type": "collection", "options": {}}"""))
    assertEquals(options, CollectionOptions(capped = Some(false)))
  }
}
