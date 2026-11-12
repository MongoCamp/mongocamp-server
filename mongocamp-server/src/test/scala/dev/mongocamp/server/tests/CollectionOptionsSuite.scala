package dev.mongocamp.server.tests

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.CollectionOptionsApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import io.circe.Json
import io.circe.parser.parse

// create a collection with options, read and change them
class CollectionOptionsSuite extends MongoCampBaseServerSuite {

  private lazy val optionsApi    = CollectionOptionsApi()
  private lazy val collectionApi = CollectionApi()
  private lazy val documentApi   = DocumentApi()

  private val validatorBody =
    """{"validator": {"$jsonSchema": {"bsonType": "object", "required": ["name"], "properties": {"name": {"bsonType": "string"}}}}, "validationLevel": "strict", "validationAction": "error"}"""

  private def json(text: String): Json = parse(text).fold(throw _, identity)

  // a missing option is written as null
  private def present(options: Json, field: String): Boolean = options.hcursor.downField(field).focus.exists(!_.isNull)

  private def create(name: String, body: Option[String] = None, token: => String = adminBearerToken) =
    executeRequest(optionsApi.createCollection("", "", token, "")(name, body.map(json)))

  private def options(name: String, token: => String = adminBearerToken) =
    executeRequestToResponse(optionsApi.getCollectionOptions("", "", token, "")(name))

  private def countOf(name: String): Long = MongoDatabase.databaseProvider.database().getCollection(name).countDocuments().result()

  private def names = executeRequestToResponse(collectionApi.listCollections("", "", adminBearerToken, "")()).toSet

  test("a collection is created without a body") {
    val response = create("optionsPlain")
    assertEquals(response.code.code, 200)
    assertEquals(response.body.toOption.flatMap(_.hcursor.get[String]("name").toOption), Some("optionsPlain"))
    assert(names.contains("optionsPlain"))
    val plain = options("optionsPlain")
    assertEquals(plain.hcursor.get[Boolean]("capped"), Right(false))
    assertEquals(present(plain, "validator"), false)
  }

  test("an existing collection is a conflict") {
    val response = create("optionsPlain")
    assertEquals(response.code.code, 409)
    assertEquals(response.header("x-error-code"), Some("4091"))
  }

  test("a capped collection keeps the options and the newest documents") {
    assertEquals(create("optionsCapped", Some("""{"capped": true, "sizeInBytes": 4096, "maxDocuments": 5}""")).code.code, 200)
    val capped = options("optionsCapped")
    assertEquals(capped.hcursor.get[Boolean]("capped"), Right(true))
    assertEquals(capped.hcursor.get[Long]("sizeInBytes"), Right(4096L))
    assertEquals(capped.hcursor.get[Long]("maxDocuments"), Right(5L))
    val docs: Seq[Map[String, Any]] = (1 to 8).map(
      i => Map[String, Any]("n" -> i)
    )
    executeRequestToResponse(documentApi.insertMany("", "", adminBearerToken, "")("optionsCapped", docs))
    assertEquals(countOf("optionsCapped"), 5L)
  }

  test("a validator is kept and a document that it refuses is a bad request without its values") {
    assertEquals(create("optionsValidated", Some(validatorBody)).code.code, 200)
    val validated = options("optionsValidated")
    assertEquals(validated.hcursor.get[String]("validationLevel"), Right("strict"))
    assertEquals(validated.hcursor.get[String]("validationAction"), Right("error"))
    assert(validated.hcursor.downField("validator").downField("$jsonSchema").focus.exists(!_.isNull), validated.noSpaces)
    assertEquals(executeRequest(documentApi.insert("", "", adminBearerToken, "")("optionsValidated", Map[String, Any]("name" -> "fine"))).code.code, 200)
    // a number where the schema wants a text: the details of the database name the value, they must not be in the answer
    val single = executeRequest(documentApi.insert("", "", adminBearerToken, "")("optionsValidated", Map[String, Any]("name" -> 987654321)))
    assertEquals(single.code.code, 400)
    assert(!single.header("x-error-message").exists(_.contains("987654321")), single.headers.toString)
    val bulk = executeRequest(documentApi.insertMany("", "", adminBearerToken, "")("optionsValidated", Seq(Map[String, Any]("name" -> 987654321))))
    assertEquals(bulk.code.code, 400)
    assert(!bulk.header("x-error-message").exists(_.contains("987654321")), bulk.headers.toString)
    assertEquals(countOf("optionsValidated"), 1L)
  }

  test("the validation can be changed") {
    val warn = executeRequestToResponse(optionsApi.modifyCollectionOptions("", "", adminBearerToken, "")("optionsValidated", json("""{"validationAction": "warn"}""")))
    assertEquals(warn.hcursor.get[String]("validationAction"), Right("warn"))
    // the warning goes to the log of the database, the document is stored
    assertEquals(executeRequest(documentApi.insert("", "", adminBearerToken, "")("optionsValidated", Map[String, Any]("name" -> 5))).code.code, 200)
    val off = executeRequestToResponse(optionsApi.modifyCollectionOptions("", "", adminBearerToken, "")("optionsValidated", json("""{"validationLevel": "off"}""")))
    assertEquals(off.hcursor.get[String]("validationLevel"), Right("off"))
    assert(present(off, "validator"), "the validator is kept")
  }

  test("what cannot be changed and what does not fit is a bad request") {
    List("""{"capped": true}""", """{"collation": {"locale": "en"}}""", """{}""").foreach(
      body => {
        val response = executeRequest(optionsApi.modifyCollectionOptions("", "", adminBearerToken, "")("optionsValidated", json(body)))
        assertEquals(response.code.code, 400, body)
        assertEquals(response.header("x-error-code"), Some("4009"), body)
      }
    )
    List("""{"capped": true}""", """{"sizeInBytes": 100}""", """{"validationLevel": "hard"}""", """{"collation": {"locale": " "}}""").foreach(
      body => assertEquals(create("optionsBad", Some(body)).code.code, 400, body)
    )
    assertEquals(names.contains("optionsBad"), false)
  }

  test("a name that is not allowed is a bad request") {
    List("system.optionsX", "options$X", "a" * 121).foreach(
      name => {
        val response = create(name)
        assertEquals(response.code.code, 400, name)
        assertEquals(response.header("x-error-code"), Some("4007"), name)
      }
    )
  }

  test("the options of a collection that does not exist are a 404") {
    assertEquals(executeRequest(optionsApi.getCollectionOptions("", "", adminBearerToken, "")("optionsNotThere")).code.code, 404)
    assertEquals(executeRequest(optionsApi.modifyCollectionOptions("", "", adminBearerToken, "")("optionsNotThere", json("""{"validationLevel": "off"}"""))).code.code, 404)
  }

  test("a collection of another database is created and read with its full name") {
    assertEquals(create("geodata:optionsOther", Some(validatorBody)).code.code, 200)
    try {
      val other = options("geodata:optionsOther")
      assert(present(other, "validator"), other.noSpaces)
      assert(MongoDatabase.databaseProvider.collectionNames("geodata").contains("optionsOther"))
      assertEquals(create("geodata:optionsOther").code.code, 409)
    }
    finally executeRequest(collectionApi.deleteCollection("", "", adminBearerToken, "")("geodata:optionsOther"))
  }

  test("the grants: administrate to create and to change, read to look") {
    // the test user has the administrate grant for `test`, the write grant for `accounts` and nothing for `optionsDenied`
    assertEquals(create("test", token = testUserBearerToken).code.code, 409)
    assertEquals(create("optionsDenied", token = testUserBearerToken).code.code, 401)
    assertEquals(names.contains("optionsDenied"), false)
    assertEquals(executeRequest(optionsApi.getCollectionOptions("", "", testUserBearerToken, "")("accounts")).code.code, 200)
    assertEquals(executeRequest(optionsApi.getCollectionOptions("", "", testUserBearerToken, "")("pokemon")).code.code, 401)
    val change = executeRequest(optionsApi.modifyCollectionOptions("", "", testUserBearerToken, "")("accounts", json("""{"validationLevel": "off"}""")))
    assertEquals(change.code.code, 401)
  }

  test("cleanup") {
    List("optionsPlain", "optionsCapped", "optionsValidated").foreach(
      name => executeRequest(collectionApi.deleteCollection("", "", adminBearerToken, "")(name))
    )
  }
}
