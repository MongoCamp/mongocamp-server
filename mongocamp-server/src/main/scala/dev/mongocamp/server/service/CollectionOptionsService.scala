package dev.mongocamp.server.service

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.client.model.ValidationAction
import com.mongodb.client.model.ValidationLevel
import com.mongodb.client.model.ValidationOptions
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.driver.mongodb.bson.BsonConverter
import dev.mongocamp.server.converter.MongoCampBsonConverter
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.CollectionCollation
import dev.mongocamp.server.model.CollectionOptions
import org.bson.BsonDocument
import org.bson.BsonValue
import org.mongodb.scala.bson.collection.immutable.Document
import scala.jdk.CollectionConverters._
import sttp.model.StatusCode

/** Creating a collection with options (capped, validator, collation) and changing its validation, and reading the options back from `listCollections`. */
object CollectionOptionsService {

  val ValidationLevels: Set[String]  = Set("off", "moderate", "strict")
  val ValidationActions: Set[String] = Set("error", "warn")

  private def invalid(message: String) = MongoCampException.badRequestException(message, ErrorCodes.invalidCollectionOptions)

  def checkForCreate(options: CollectionOptions): Unit = {
    val capped = options.capped.getOrElse(false)
    if (capped && !options.sizeInBytes.exists(_ > 0)) throw invalid("a capped collection needs sizeInBytes greater than 0")
    if (!capped && (options.sizeInBytes.isDefined || options.maxDocuments.isDefined))
      throw invalid("sizeInBytes and maxDocuments are only for a capped collection (capped true)")
    if (options.maxDocuments.exists(_ <= 0)) throw invalid("maxDocuments has to be greater than 0")
    checkValidation(options)
    options.collation.foreach(checkCollation)
  }

  def checkForModify(options: CollectionOptions): Unit = {
    if (options.capped.isDefined || options.sizeInBytes.isDefined || options.maxDocuments.isDefined || options.collation.isDefined)
      throw invalid(
        "capped, sizeInBytes, maxDocuments and collation cannot be changed after the creation, only validator, validationLevel and validationAction"
      )
    if (options.validator.isEmpty && options.validationLevel.isEmpty && options.validationAction.isEmpty)
      throw invalid("nothing to change: send validator, validationLevel or validationAction")
    checkValidation(options)
  }

  private def checkValidation(options: CollectionOptions): Unit = {
    options.validationLevel.foreach(
      level => if (!ValidationLevels.contains(level)) throw invalid(s"validationLevel has to be one of ${ValidationLevels.toList.sorted.mkString(", ")}")
    )
    options.validationAction.foreach(
      action => if (!ValidationActions.contains(action)) throw invalid(s"validationAction has to be one of ${ValidationActions.toList.sorted.mkString(", ")}")
    )
  }

  private def checkCollation(collation: CollectionCollation): Unit = {
    if (collation.locale.isBlank) throw invalid("the locale of the collation is empty")
    collation.strength.foreach(
      strength => if (strength < 1 || strength > 5) throw invalid("the strength of the collation has to be between 1 and 5")
    )
  }

  private def validatorOf(validator: Map[String, Any]): Document = documentFromScalaMap(MongoCampBsonConverter.convertFields(validator))

  def toCreateCollectionOptions(options: CollectionOptions): CreateCollectionOptions = {
    val result = new CreateCollectionOptions()
    if (options.capped.getOrElse(false)) {
      result.capped(true)
      options.sizeInBytes.foreach(
        size => result.sizeInBytes(size)
      )
      options.maxDocuments.foreach(
        max => result.maxDocuments(max)
      )
    }
    if (options.validator.isDefined || options.validationLevel.isDefined || options.validationAction.isDefined) {
      val validation = new ValidationOptions()
      options.validator.foreach(
        validator => validation.validator(validatorOf(validator))
      )
      options.validationLevel.foreach(
        level => validation.validationLevel(ValidationLevel.fromString(level))
      )
      options.validationAction.foreach(
        action => validation.validationAction(ValidationAction.fromString(action))
      )
      result.validationOptions(validation)
    }
    options.collation.foreach(
      collation => result.collation(toCollation(collation))
    )
    result
  }

  private def toCollation(collation: CollectionCollation): Collation = {
    val builder = Collation.builder().locale(collation.locale)
    collation.strength.foreach(
      strength => builder.collationStrength(CollationStrength.fromInt(strength))
    )
    collation.caseLevel.foreach(
      caseLevel => builder.caseLevel(caseLevel)
    )
    collation.numericOrdering.foreach(
      numericOrdering => builder.numericOrdering(numericOrdering)
    )
    builder.build()
  }

  /** The command that changes the validation of a collection. */
  def collModCommand(name: String, options: CollectionOptions): Document = {
    var command = Document("collMod" -> name)
    options.validator.foreach(
      validator => command = command ++ Document("validator" -> validatorOf(validator))
    )
    options.validationLevel.foreach(
      level => command = command ++ Document("validationLevel" -> level)
    )
    options.validationAction.foreach(
      action => command = command ++ Document("validationAction" -> action)
    )
    command
  }

  private def numberOf(document: BsonDocument, key: String): Option[Long] =
    Option(document.get(key)).filter(_.isNumber).map(_.asNumber().longValue())

  private def textOf(document: BsonDocument, key: String): Option[String] =
    Option(document.get(key)).filter(_.isString).map(_.asString().getValue)

  /** The options of the entry of a collection in the answer of `listCollections` (its field `options`). */
  def parseOptions(entry: BsonDocument): CollectionOptions = {
    val options = Option(entry.get("options")).filter(_.isDocument).map(_.asDocument()).getOrElse(new BsonDocument())
    val capped = Option(options.get("capped")).exists(
      value => value.isBoolean && value.asBoolean().getValue
    )
    val validator = Option(options.get("validator"))
      .filter(_.isDocument)
      .map(
        value => documentToMap(value.asDocument())
      )
    val collation = Option(options.get("collation"))
      .filter(_.isDocument)
      .map(_.asDocument())
      .flatMap(
        document =>
          textOf(document, "locale").map(
            locale =>
              CollectionCollation(
                locale,
                numberOf(document, "strength").map(_.toInt),
                Option(document.get("caseLevel")).filter(_.isBoolean).map(_.asBoolean().getValue),
                Option(document.get("numericOrdering")).filter(_.isBoolean).map(_.asBoolean().getValue)
              )
          )
      )
    CollectionOptions(
      Some(capped),
      if (capped) numberOf(options, "size") else None,
      if (capped) numberOf(options, "max") else None,
      validator,
      textOf(options, "validationLevel"),
      textOf(options, "validationAction"),
      collation
    )
  }

  /** A validator holds documents, lists, texts, numbers and booleans: they are converted here, everything else is left to the converter of the driver. */
  private def documentToMap(document: BsonDocument): Map[String, Any] =
    document.asScala.toMap.map(
      entry => entry._1 -> toScala(entry._2)
    )

  private def toScala(value: BsonValue): Any =
    if (value.isDocument) documentToMap(value.asDocument())
    else if (value.isArray) value.asArray().getValues.asScala.toList.map(toScala)
    else if (value.isString) value.asString().getValue
    else if (value.isBoolean) value.asBoolean().getValue
    else if (value.isInt32) value.asInt32().getValue
    else if (value.isInt64) value.asInt64().getValue
    else if (value.isDouble) value.asDouble().getValue
    else if (value.isNull) null
    else BsonConverter.fromBson(value)

  private def notFound = MongoCampException("collection not found", StatusCode.NotFound)

  /** The options of a collection, 404 for a collection that does not exist. */
  def read(collection: String): CollectionOptions = {
    val name     = CollectionNameService.parse(collection)
    val database = MongoDatabase.databaseProvider.database(name.database.getOrElse(MongoDatabase.databaseProvider.DefaultDatabaseName))
    val answer = database
      .runCommand(Document("listCollections" -> 1, "filter" -> Document("name" -> name.name)))
      .result()
    val entries = answer
      .get("cursor")
      .filter(_.isDocument)
      .flatMap(
        cursor => Option(cursor.asDocument().get("firstBatch"))
      )
      .filter(_.isArray)
      .map(_.asArray().getValues.asScala.toList.filter(_.isDocument).map(_.asDocument()))
      .getOrElse(Nil)
    parseOptions(entries.headOption.getOrElse(throw notFound))
  }

  /** Changes the validation of a collection and answers the options after the change. */
  def modify(collection: String, options: CollectionOptions): CollectionOptions = {
    val name     = CollectionNameService.parse(collection)
    val provider = MongoDatabase.databaseProvider
    val database = name.database.getOrElse(provider.DefaultDatabaseName)
    if (!provider.collectionNames(database).contains(name.name)) throw notFound
    provider.database(database).runCommand(collModCommand(name.name, options)).result()
    read(collection)
  }
}
