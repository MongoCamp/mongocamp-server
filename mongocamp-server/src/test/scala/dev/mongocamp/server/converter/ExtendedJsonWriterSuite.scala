package dev.mongocamp.server.converter

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.exception.MongoCampException
import io.circe.Json
import org.bson.BsonDocument

class ExtendedJsonWriterSuite extends munit.FunSuite {

  // the decoder of the request bodies of the server and the test client
  private object Decoder extends CirceSchema

  // every type of the database, written as canonical Extended JSON that the driver reads
  private val original = BsonDocument.parse("""{
      |  "_id": {"$oid": "507f1f77bcf86cd799439011"},
      |  "int": {"$numberInt": "5"},
      |  "long": {"$numberLong": "9007199254740993"},
      |  "double": {"$numberDouble": "2.0"},
      |  "fraction": {"$numberDouble": "1.5"},
      |  "decimal": {"$numberDecimal": "10.25"},
      |  "date": {"$date": {"$numberLong": "1718452800000"}},
      |  "oid": {"$oid": "507f1f77bcf86cd799439012"},
      |  "binary": {"$binary": {"base64": "AQID", "subType": "00"}},
      |  "uuid": {"$binary": {"base64": "Ej5FZ+ibEtOkVkJmFEAAAA==", "subType": "04"}},
      |  "timestamp": {"$timestamp": {"t": 1700000000, "i": 3}},
      |  "regex": {"$regularExpression": {"pattern": "^a", "options": "i"}},
      |  "min": {"$minKey": 1},
      |  "max": {"$maxKey": 1},
      |  "text": "2024-06-15T12:00:00Z",
      |  "flag": true,
      |  "nothing": null,
      |  "nested": {"list": [{"$numberInt": "1"}, "a", {"$numberDecimal": "2"}], "child": {"n": {"$numberLong": "7"}}}
      |}""".stripMargin)

  test("canonical: every number has a wrapper") {
    val map = ExtendedJsonWriter.toMap(original, ExtendedJsonWriter.Canonical)
    assertEquals(map("_id"), Map("$oid" -> "507f1f77bcf86cd799439011"))
    assertEquals(map("int"), Map("$numberInt" -> "5"))
    assertEquals(map("long"), Map("$numberLong" -> "9007199254740993"))
    assertEquals(map("double"), Map("$numberDouble" -> "2.0"))
    assertEquals(map("decimal"), Map("$numberDecimal" -> "10.25"))
    assertEquals(map("date"), Map("$date" -> Map("$numberLong" -> "1718452800000")))
    assertEquals(map("timestamp"), Map("$timestamp" -> Map("t" -> 1700000000L, "i" -> 3L)))
    assertEquals(map("min"), Map("$minKey" -> 1L))
    // a text that looks like a date is marked, a plain ISO string would be read as a date
    assertEquals(map("text"), Map("$string" -> "2024-06-15T12:00:00Z"))
    assertEquals(map("flag"), true)
    assertEquals(map("nothing"), null)
    assertEquals(map("nested").asInstanceOf[Map[String, Any]]("list"), List(Map("$numberInt" -> "1"), "a", Map("$numberDecimal" -> "2")))
  }

  test("relaxed: numbers are numbers, the other types have a wrapper") {
    val map = ExtendedJsonWriter.toMap(original, ExtendedJsonWriter.Relaxed)
    assertEquals(map("int"), 5L)
    assertEquals(map("long"), 9007199254740993L)
    assertEquals(map("fraction"), 1.5)
    assertEquals(map("decimal"), Map("$numberDecimal" -> "10.25"))
    assertEquals(map("date"), Map("$date" -> "2024-06-15T12:00:00Z"))
    assertEquals(map("_id"), Map("$oid" -> "507f1f77bcf86cd799439011"))
    assertEquals(map("text"), Map("$string" -> "2024-06-15T12:00:00Z"))
    assertEquals(map("nested").asInstanceOf[Map[String, Any]]("list"), List(1L, "a", Map("$numberDecimal" -> "2")))
  }

  test("what is read in canonical mode can be written as it is: the document in the database has the same types") {
    val map       = ExtendedJsonWriter.toMap(original, ExtendedJsonWriter.Canonical)
    val converted = documentFromScalaMap(MongoCampBsonConverter.convertFields(map)).toBsonDocument
    assertEquals(converted, original)
  }

  test("the mode of the query parameter") {
    assertEquals(ExtendedJsonWriter.parseMode(None), None)
    assertEquals(ExtendedJsonWriter.parseMode(Some("")), None)
    assertEquals(ExtendedJsonWriter.parseMode(Some("relaxed")), Some(ExtendedJsonWriter.Relaxed))
    assertEquals(ExtendedJsonWriter.parseMode(Some(" Canonical ")), Some(ExtendedJsonWriter.Canonical))
    val exception = intercept[MongoCampException](ExtendedJsonWriter.parseMode(Some("legacy")))
    assertEquals(exception.statusCode.code, 400)
    assert(exception.getMessage.contains("relaxed or canonical"), exception.getMessage)
  }

  test("a text with the wrapper $string stays a text, in the decoder of the requests and in the converter") {
    val json    = Json.obj("a" -> Json.obj("$string" -> Json.fromString("2024-06-15T12:00:00Z")), "b" -> Json.fromString("2024-06-15T12:00:00Z"))
    val decoded = Decoder.decodeFromJson(json).asInstanceOf[Map[String, Any]]
    assertEquals(decoded("a"), Map("$string" -> "2024-06-15T12:00:00Z"))
    assert(decoded("b").isInstanceOf[org.joda.time.DateTime], s"${decoded("b")}")
    val converted = MongoCampBsonConverter.convertFields(decoded)
    assertEquals(converted("a"), "2024-06-15T12:00:00Z")
    assert(converted("b").isInstanceOf[java.util.Date])
    val nested = MongoCampBsonConverter.convertFields(Map("c" -> List(Map("$string" -> "x"), Map("d" -> Map("$string" -> "y")))))
    assertEquals(nested("c"), List("x", Map("d" -> "y")))
    intercept[MongoCampException](MongoCampBsonConverter.convertFields(Map("c" -> Map("$string" -> 5))))
  }
}
