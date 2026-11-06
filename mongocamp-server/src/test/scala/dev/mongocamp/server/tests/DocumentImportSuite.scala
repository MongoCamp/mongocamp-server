package dev.mongocamp.server.tests

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.service.ConfigurationService
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import dev.mongocamp.server.test.TestServer
import io.circe.parser.parse
import io.circe.Json
import org.bson.BsonDocument
import org.bson.BsonType
import org.mongodb.scala.bson.collection.immutable.Document
import sttp.client3._
import sttp.model.Uri

// the import of documents from a stream of lines (NDJSON)
class DocumentImportSuite extends MongoCampBaseServerSuite {

  private val target = "importTarget"

  private def provider = MongoDatabase.databaseProvider

  private def send(path: String, body: String, token: String = adminBearerToken, name: String = target) = {
    val uri      = Uri.unsafeParse(TestServer.serverBaseUrl + "/mongodb/collections/" + name + path)
    val response = TestAdditions.backend.send(basicRequest.post(uri).header("Authorization", "Bearer " + token).contentType("application/x-ndjson").body(body))
    (response.code.code, parse(response.body.fold(identity, identity)).getOrElse(Json.Null), response)
  }

  private def importLines(body: String, token: String = adminBearerToken, name: String = target) = send("/documents/import", body, token, name)

  private def count(name: String = target) = provider.dao(name).count().result()

  private def raw(id: String, name: String = target): BsonDocument =
    provider.collection(name).find[BsonDocument]().resultList().find(d => d.get("_id").toString.contains(id)).getOrElse(fail(s"no document $id"))

  private def numbers(json: Json) = {
    val cursor = json.hcursor
    (cursor.get[Long]("read").toOption.get, cursor.get[Long]("inserted").toOption.get, cursor.get[Long]("failed").toOption.get)
  }

  test("the lines of the body are stored") {
    val (code, json, _) = importLines("""{"_id": "a", "n": 1}""" + "\n" + """{"_id": "b", "n": 2}""" + "\n" + """{"_id": "c", "n": 3}""" + "\n")
    assertEquals(code, 200, json.noSpaces)
    assertEquals(numbers(json), (3L, 3L, 0L))
    assertEquals(json.hcursor.get[List[Json]]("errors").toOption.map(_.size), Some(0))
    assertEquals(count(), 3L)
  }

  test("blank lines are skipped, a line ends with LF or CRLF, the last line needs no line break, the lines are counted with the blank ones") {
    val body            = "{\"_id\": \"d\"}\r\n\r\n   \n{\"_id\": \"e\"}\r\n{broken\n{\"_id\": \"f\"}"
    val (code, json, _) = importLines(body)
    assertEquals(code, 200, json.noSpaces)
    assertEquals(numbers(json), (4L, 3L, 1L))
    val errors = json.hcursor.downField("errors").downArray
    assertEquals(errors.get[Int]("index"), Right(5))
    assertEquals(errors.get[Int]("code"), Right(ErrorCodes.invalidImportLine))
    assertEquals(count(), 6L)
  }

  test("a line that cannot be stored does not stop the others, it is named with its line") {
    val body            = List("""{"_id": "a", "again": true}""", """{"_id": "g"}""", """[1]""", """{"_id": "h", "n": {"$numberInt": "1.5"}}""", """{"_id": "b"}""", """{"_id": "i"}""").mkString("\n")
    val (code, json, _) = importLines(body)
    assertEquals(code, 200, json.noSpaces)
    assertEquals(numbers(json), (6L, 2L, 4L))
    val errors = json.hcursor.get[List[Json]]("errors").toOption.get.map(e => (e.hcursor.get[Int]("index").toOption.get, e.hcursor.get[Int]("code").toOption.get))
    assertEquals(errors.map(_._1), List(1, 3, 4, 5))
    assertEquals(errors.find(_._1 == 1).map(_._2), Some(11000))
    assertEquals(errors.find(_._1 == 3).map(_._2), Some(ErrorCodes.invalidImportLine))
    // the message of a duplicate key does not name the database, the collection or the value
    val duplicate = json.hcursor.downField("errors").downArray.get[String]("message").toOption.get
    assert(!duplicate.contains(target) && !duplicate.contains("dup key"), duplicate)
    assertEquals(count(), 8L)
  }

  test("the values are read like in an insert: wrappers, dates and the text marker") {
    val line = """{"_id": "typed", "int": {"$numberInt": "5"}, "dec": {"$numberDecimal": "10.25"}, "when": "2024-06-15T12:00:00Z", "asText": {"$string": "2024-06-15T12:00:00Z"}}"""
    assertEquals(numbers(importLines(line)._2), (1L, 1L, 0L))
    val stored = raw("typed")
    assertEquals(stored.get("int").getBsonType, BsonType.INT32)
    assertEquals(stored.get("dec").getBsonType, BsonType.DECIMAL128)
    assertEquals(stored.get("when").getBsonType, BsonType.DATE_TIME)
    assertEquals(stored.get("asText").getBsonType, BsonType.STRING)
  }

  test("a document without an id gets one") {
    val before = count()
    assertEquals(numbers(importLines("""{"n": 1}""" + "\n" + """{"n": 2}""")._2), (2L, 2L, 0L))
    assertEquals(count(), before + 2)
  }

  test("an empty body is an import of nothing") {
    val (code, json, _) = importLines("")
    assertEquals(code, 200)
    assertEquals(numbers(json), (0L, 0L, 0L))
  }

  test("what the export writes can be imported: the documents have the same types") {
    val source = "importRoundTripSource"
    provider.dao(source).insertMany(Seq(Document("_id" -> "r1", "n" -> 5, "big" -> 9007199254740993L, "d" -> 1.5, "when" -> new java.util.Date(1718452800000L), "text" -> "plain"))).result()
    provider.dao(source).insertOne(Document("_id" -> "r2", "list" -> Seq(1, 2), "nested" -> Document("x" -> 1L))).result()
    val exported   = TestAdditions.backend.send(
      basicRequest.get(Uri.unsafeParse(TestServer.serverBaseUrl + "/mongodb/collections/" + source + "/documents/export?extendedJson=canonical")).header("Authorization", "Bearer " + adminBearerToken)
    )
    val (code, json, _) = importLines(exported.body.fold(identity, identity), name = "importRoundTripTarget")
    assertEquals(code, 200, json.noSpaces)
    assertEquals(numbers(json), (2L, 2L, 0L))
    List("r1", "r2").foreach(
      id => assertEquals(raw(id, "importRoundTripTarget"), raw(id, source), s"document $id")
    )
    provider.dao(source).drop().result()
    provider.dao("importRoundTripTarget").drop().result()
  }

  test("a body that is greater than the limit of the HTTP server (8 MB) is imported, in many batches") {
    val line   = (1 to 20).map(i => s""""f$i": "${"x" * 20}"""").mkString("{\"n\": %d, ", ", ", "}")
    val lines  = (1 to 30000).map(i => line.format(i))
    val body   = lines.mkString("\n")
    assert(body.length > 12 * 1024 * 1024, s"the body has ${body.length} characters")
    val before          = count()
    val (code, json, _) = importLines(body)
    assertEquals(code, 200, json.noSpaces.take(300))
    assertEquals(numbers(json), (30000L, 30000L, 0L))
    assertEquals(count(), before + 30000)
  }

  test("the size of the body is limited by IMPORT_MAX_BYTES, 413 above, the configuration is read for every request") {
    val key = DefaultConfigurations.ConfigKeyImportMaxBytes
    assertEquals(ConfigurationService.updateConfig(key, 2000L), true)
    try {
      val before = count()
      val big    = (1 to 200).map(i => s"""{"n": $i, "text": "${"y" * 50}"}""").mkString("\n")
      assert(big.length > 2000)
      val (code, _, _) = importLines(big)
      assertEquals(code, 413)
      val small = importLines("""{"_id": "limitOk"}""")
      assertEquals(small._1, 200)
      assert(count() <= before + 200 + 1)
    }
    finally ConfigurationService.updateConfig(key, 1073741824L)
    assertEquals(importLines("""{"_id": "afterLimit"}""")._1, 200)
  }

  test("the errors that are listed are limited, the number of failed lines is not") {
    val body            = (1 to 1500).map(i => s"broken $i").mkString("\n")
    val (code, json, _) = importLines(body)
    assertEquals(code, 200)
    assertEquals(numbers(json), (1500L, 0L, 1500L))
    assertEquals(json.hcursor.get[List[Json]]("errors").toOption.map(_.size), Some(1000))
    assertEquals(json.hcursor.get[Boolean]("errorsTruncated"), Right(true))
  }

  test("the write grant is needed") {
    val noWrite = importLines("""{"_id": "x"}""", token = testUserBearerToken, name = "deleteTest")._1
    assert(Set(401, 403).contains(noWrite), s"unexpected status $noWrite")
    val noGrant = importLines("""{"_id": "x"}""", token = testUserBearerToken, name = "pokemon")._1
    assert(Set(401, 403).contains(noGrant), s"unexpected status $noGrant")
    assertEquals(importLines("""{"_id": "importUserDoc"}""", token = testUserBearerToken, name = "test")._1, 200)
    provider.dao("test").deleteOne(Document("_id" -> "importUserDoc")).result()
  }

  test("cleanup") {
    provider.dao(target).drop().result()
  }
}
