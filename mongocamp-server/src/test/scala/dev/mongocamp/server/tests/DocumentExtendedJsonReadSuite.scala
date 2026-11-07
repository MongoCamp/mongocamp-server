package dev.mongocamp.server.tests

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import dev.mongocamp.server.test.TestServer
import io.circe.parser.parse
import io.circe.Json
import org.bson.BsonDocument
import org.bson.BsonType
import sttp.client3._
import sttp.model.Uri

// documents with the types of their values (extendedJson=relaxed|canonical) and a text that looks like a date ({"$string": ...}), checked on the HTTP answers
class DocumentExtendedJsonReadSuite extends MongoCampBaseServerSuite {

  private val collection = "extendedJsonReadTest"
  private val oid        = "507f1f77bcf86cd799439011"

  private def url(path: String) = Uri.unsafeParse(TestServer.serverBaseUrl + "/mongodb/collections/" + collection + path)

  private def send(request: Request[Either[String, String], Any]) = {
    val response = TestAdditions.backend.send(request.header("Authorization", "Bearer " + adminBearerToken).contentType("application/json"))
    (response.code.code, response.body.fold(identity, identity), response)
  }

  private def get(path: String): (Int, Json) = {
    val (code, body, _) = send(basicRequest.get(url(path)))
    (code, parse(body).getOrElse(Json.Null))
  }

  private def put(path: String, body: String): (Int, Json) = {
    val (code, text, _) = send(basicRequest.put(url(path)).body(body))
    (code, parse(text).getOrElse(Json.Null))
  }

  private def post(path: String, body: String): (Int, Json) = {
    val (code, text, _) = send(basicRequest.post(url(path)).body(body))
    (code, parse(text).getOrElse(Json.Null))
  }

  private def raw(id: String): BsonDocument =
    MongoDatabase.databaseProvider
      .collection(collection)
      .find[BsonDocument]()
      .resultList()
      .find(
        d => d.get("_id").toString.contains(id)
      )
      .getOrElse(fail(s"no document $id"))

  private val typed =
    s"""{"_id": {"$$oid": "$oid"}, "int": {"$$numberInt": "5"}, "long": {"$$numberLong": "9007199254740993"}, "double": {"$$numberDouble": "2.0"},
       |"decimal": {"$$numberDecimal": "10.25"}, "when": {"$$date": "2024-06-15T12:00:00Z"}, "text": "plain",
       |"asText": {"$$string": "2024-06-15T12:00:00Z"}, "bin": {"$$binary": {"base64": "AQID", "subType": "00"}},
       |"nested": {"n": {"$$numberInt": "1"}}, "list": [{"$$numberInt": "1"}, "a"]}""".stripMargin

  test("prepare: a document with every kind of value") {
    val (code, answer) = put("/documents", typed)
    assertEquals(code, 200, answer.noSpaces)
    val stored = raw(oid)
    assertEquals(stored.get("int").getBsonType, BsonType.INT32)
    assertEquals(stored.get("long").getBsonType, BsonType.INT64)
    assertEquals(stored.get("when").getBsonType, BsonType.DATE_TIME)
  }

  test("without the parameter the document is as before") {
    val (code, json) = get(s"/documents/$oid")
    assertEquals(code, 200)
    val cursor = json.hcursor
    assertEquals(cursor.get[String]("_id"), Right(oid))
    assertEquals(cursor.get[Int]("int"), Right(5))
    assertEquals(cursor.get[String]("when"), Right("2024-06-15T12:00:00Z"))
    assertEquals(cursor.get[String]("asText"), Right("2024-06-15T12:00:00Z"))
  }

  test("relaxed") {
    val (code, json) = get(s"/documents/$oid?extendedJson=relaxed")
    assertEquals(code, 200)
    val cursor = json.hcursor
    assertEquals(cursor.downField("_id").get[String]("$oid"), Right(oid))
    assertEquals(cursor.get[Int]("int"), Right(5))
    assertEquals(cursor.get[Long]("long"), Right(9007199254740993L))
    assertEquals(cursor.downField("decimal").get[String]("$numberDecimal"), Right("10.25"))
    assertEquals(cursor.downField("when").get[String]("$date"), Right("2024-06-15T12:00:00Z"))
    // a text that looks like a date is marked, it is not a date and not a plain string that is read as one
    assertEquals(cursor.downField("asText").get[String]("$string"), Right("2024-06-15T12:00:00Z"))
    assertEquals(cursor.downField("bin").downField("$binary").get[String]("base64"), Right("AQID"))
    assertEquals(cursor.downField("nested").get[Int]("n"), Right(1))
  }

  test("canonical") {
    val (code, json) = get(s"/documents/$oid?extendedJson=canonical")
    assertEquals(code, 200)
    val cursor = json.hcursor
    assertEquals(cursor.downField("int").get[String]("$numberInt"), Right("5"))
    assertEquals(cursor.downField("long").get[String]("$numberLong"), Right("9007199254740993"))
    assertEquals(cursor.downField("double").get[String]("$numberDouble"), Right("2.0"))
    assertEquals(cursor.downField("when").downField("$date").get[String]("$numberLong"), Right("1718452800000"))
    assertEquals(cursor.downField("nested").downField("n").get[String]("$numberInt"), Right("1"))
    assertEquals(cursor.downField("asText").get[String]("$string"), Right("2024-06-15T12:00:00Z"))
    assertEquals(cursor.get[String]("text"), Right("plain"))
  }

  test("the list, the find, the search and the aggregation have the parameter too") {
    val list = get("/documents?extendedJson=canonical")
    assertEquals(list._1, 200)
    assertEquals(list._2.hcursor.downArray.downField("int").get[String]("$numberInt"), Right("5"))
    val find = post("/documents?extendedJson=relaxed", """{"filter": {"int": {"$numberInt": "5"}}, "sort": {}, "projection": {}}""")
    assertEquals(find._1, 200)
    assertEquals(find._2.hcursor.downArray.downField("_id").get[String]("$oid"), Right(oid))
    val search = post("/documents/search?extendedJson=canonical", """{"filter": "text:plain"}""")
    assertEquals(search._1, 200)
    assertEquals(search._2.hcursor.downArray.downField("long").get[String]("$numberLong"), Right("9007199254740993"))
    val aggregate = post("/aggregate?extendedJson=canonical", """{"pipeline": [{"stage": "project", "value": {"int": 1, "when": 1}}], "allowDiskUse": false}""")
    assertEquals(aggregate._1, 200)
    assertEquals(aggregate._2.hcursor.downArray.downField("int").get[String]("$numberInt"), Right("5"))
    // without the parameter the aggregation is as before
    assertEquals(
      post("/aggregate", """{"pipeline": [{"stage": "project", "value": {"int": 1}}], "allowDiskUse": false}""")._2.hcursor.downArray.get[Int]("int"),
      Right(5)
    )
  }

  test("another value is a bad request") {
    assertEquals(get(s"/documents/$oid?extendedJson=legacy")._1, 400)
    assertEquals(get("/documents?extendedJson=x")._1, 400)
    assertEquals(post("/aggregate?extendedJson=x", """{"pipeline": [], "allowDiskUse": false}""")._1, 400)
  }

  test("what is read in canonical mode can be sent back as it is, the copy has the same types") {
    val (_, json) = get(s"/documents/$oid?extendedJson=canonical")
    val copy      = json.mapObject(_.add("_id", Json.fromString("copyOfTyped")))
    assertEquals(put("/documents", copy.noSpaces)._1, 200)
    val (original, stored) = (raw(oid), raw("copyOfTyped"))
    original
      .keySet()
      .toArray
      .map(_.toString)
      .filter(_ != "_id")
      .foreach(
        key => assertEquals(stored.get(key), original.get(key), s"field $key")
      )
  }

  test("a text that looks like a date is stored as a text with the wrapper $string, a plain value is a date") {
    val stored = raw(oid)
    assertEquals(stored.get("asText").getBsonType, BsonType.STRING)
    assertEquals(stored.get("asText").asString().getValue, "2024-06-15T12:00:00Z")
    assertEquals(stored.get("text").getBsonType, BsonType.STRING)
    val plain = put(
      "/documents",
      """{"_id": "dates", "plainIso": "2024-06-15T12:00:00Z", "wrapped": {"$string": "2024-06-15T12:00:00Z"}, "list": [{"$string": "2024-01-01T00:00:00Z"}], "deep": {"x": {"$string": "2024-01-01T00:00:00Z"}}}"""
    )
    assertEquals(plain._1, 200)
    val dates = raw("dates")
    assertEquals(dates.get("plainIso").getBsonType, BsonType.DATE_TIME)
    assertEquals(dates.get("wrapped").getBsonType, BsonType.STRING)
    assertEquals(dates.getArray("list").get(0).getBsonType, BsonType.STRING)
    assertEquals(dates.getDocument("deep").get("x").getBsonType, BsonType.STRING)
  }

  test("a filter finds the text, and $string needs a text") {
    val found = post("/documents", """{"filter": {"wrapped": {"$string": "2024-06-15T12:00:00Z"}}, "sort": {}, "projection": {}}""")
    assertEquals(found._1, 200)
    assertEquals(found._2.asArray.map(_.size), Some(1))
    assertEquals(found._2.hcursor.downArray.get[String]("_id"), Right("dates"))
    val noText = put("/documents", """{"_id": "bad", "v": {"$string": 5}}""")
    assertEquals(noText._1, 400)
  }

  test("cleanup") {
    executeRequestToResponse(CollectionApi().deleteCollection("", "", adminBearerToken, "")(collection))
  }
}
