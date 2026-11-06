package dev.mongocamp.server.tests

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.client.model.MongoFindRequest
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import org.bson.BsonDocument
import org.bson.BsonType

// the Extended JSON wrappers in a request body give the value its BSON type, checked in the database
class DocumentExtendedJsonSuite extends MongoCampBaseServerSuite {

  private lazy val documentsApi = DocumentApi()

  private val collection = "extendedJsonTest"

  private def raw(id: Any): BsonDocument = {
    val documents = MongoDatabase.databaseProvider.collection(collection).find[BsonDocument]().resultList()
    documents.find(d => d.get("_id").toString.contains(id.toString)).getOrElse(fail(s"no document with the id $id in ${documents.map(_.get("_id"))}"))
  }

  private def insert(document: Map[String, Any]) = executeRequest(documentsApi.insert("", "", adminBearerToken, "")(collection, document))

  test("every wrapper is stored with its BSON type") {
    val response = insert(
      Map(
        "_id"    -> "all",
        "int"    -> Map("$numberInt" -> "5"),
        "double" -> Map("$numberDouble" -> "1.5"),
        "dec"    -> Map("$numberDecimal" -> "10.25"),
        "oid"    -> Map("$oid" -> "507f1f77bcf86cd799439011"),
        "bin"    -> Map("$binary" -> Map("base64" -> "AQID", "subType" -> "00")),
        "ts"     -> Map("$timestamp" -> Map("t" -> 1700000000, "i" -> 3)),
        "re"     -> Map("$regularExpression" -> Map("pattern" -> "^a", "options" -> "i")),
        "uuid"   -> Map("$uuid" -> "123e4567-e89b-12d3-a456-426614174000"),
        "min"    -> Map("$minKey" -> 1),
        "max"    -> Map("$maxKey" -> 1),
        "nested" -> Map("list" -> List(Map("$numberInt" -> "1"), Map("$numberDecimal" -> "2")), "child" -> Map("n" -> Map("$numberInt" -> "9")))
      )
    )
    assertEquals(response.code.code, 200, response.body.fold(_.getMessage, _ => ""))
    val stored = raw("all")
    val types  = List("int", "double", "dec", "oid", "bin", "ts", "re", "uuid", "min", "max").map(f => f -> stored.get(f).getBsonType).toMap
    assertEquals(
      types,
      Map(
        "int"    -> BsonType.INT32,
        "double" -> BsonType.DOUBLE,
        "dec"    -> BsonType.DECIMAL128,
        "oid"    -> BsonType.OBJECT_ID,
        "bin"    -> BsonType.BINARY,
        "ts"     -> BsonType.TIMESTAMP,
        "re"     -> BsonType.REGULAR_EXPRESSION,
        "uuid"   -> BsonType.BINARY,
        "min"    -> BsonType.MIN_KEY,
        "max"    -> BsonType.MAX_KEY
      )
    )
    assertEquals(stored.getBinary("uuid").getType.toInt, 4)
    assertEquals(stored.getBinary("bin").getType.toInt, 0)
    assertEquals(stored.getTimestamp("ts").getTime, 1700000000)
    assertEquals(stored.getRegularExpression("re").getOptions, "i")
    assertEquals(stored.getDocument("nested").getArray("list").get(0).getBsonType, BsonType.INT32)
    assertEquals(stored.getDocument("nested").getArray("list").get(1).getBsonType, BsonType.DECIMAL128)
    assertEquals(stored.getDocument("nested").getDocument("child").get("n").getBsonType, BsonType.INT32)
  }

  test("insertMany and update use the wrappers too") {
    val many = executeRequest(documentsApi.insertMany("", "", adminBearerToken, "")(collection, Seq(Map("_id" -> "m1", "n" -> Map("$numberInt" -> "1")), Map("_id" -> "m2", "n" -> Map("$numberInt" -> "2")))))
    assertEquals(many.code.code, 200, many.body.fold(_.getMessage, _ => ""))
    assertEquals(raw("m1").get("n").getBsonType, BsonType.INT32)
    val update = executeRequest(documentsApi.updateDocumentPartial("", "", adminBearerToken, "")(collection, "m1", Map("later" -> Map("$numberDecimal" -> "3.5"))))
    assertEquals(update.code.code, 200, update.body.fold(_.getMessage, _ => ""))
    assertEquals(raw("m1").get("later").getBsonType, BsonType.DECIMAL128)
  }

  test("a wrapper in a filter has its type, also in operators") {
    def find(filter: Map[String, Any]) = executeRequestToResponse(documentsApi.find("", "", adminBearerToken, "")(collection, MongoFindRequest(filter, Map(), Map())))
    assertEquals(find(Map("n" -> Map("$numberInt" -> "2"))).map(_("_id").toString), List("m2"))
    assertEquals(find(Map("n" -> Map("$in" -> List(Map("$numberInt" -> "1"), Map("$numberInt" -> "2"))))).map(_("_id").toString).toSet, Set("m1", "m2"))
    assertEquals(find(Map("n" -> Map("$gt" -> Map("$numberInt" -> "1")))).map(_("_id").toString), List("m2"))
  }

  test("the id can have a type by a wrapper") {
    assertEquals(insert(Map("_id" -> Map("$numberInt" -> "77"), "idTest" -> true)).code.code, 200)
    assertEquals(MongoDatabase.databaseProvider.collection(collection).find[BsonDocument]().resultList().find(_.containsKey("idTest")).get.get("_id").getBsonType, BsonType.INT32)
  }

  test("a wrapper that is not valid is a bad request and nothing is stored") {
    val before = MongoDatabase.databaseProvider.dao(collection).count().result()
    List(
      Map("_id" -> "bad1", "v" -> Map("$numberInt" -> "12.5")),
      Map("_id" -> "bad2", "v" -> Map("$oid" -> "xyz")),
      Map("_id" -> "bad3", "nested" -> Map("v" -> Map("$timestamp" -> Map("t" -> 1)))),
      Map("_id" -> "bad4", "v" -> List(Map("$uuid" -> "no")))
    ).foreach(
      document => {
        val response = insert(document)
        assertEquals(response.code.code, 400, s"$document")
        assertEquals(response.header("x-error-message").exists(_.contains("$")), true, s"$document")
      }
    )
    assertEquals(MongoDatabase.databaseProvider.dao(collection).count().result(), before)
  }

  test("a map with more than the wrapper key stays a document") {
    assertEquals(insert(Map("_id" -> "plain", "v" -> Map("$numberInt" -> "9", "other" -> 1))).code.code, 200)
    val v = raw("plain").getDocument("v")
    assertEquals(v.keySet().toArray.toList, List("$numberInt", "other"))
  }

  test("cleanup") {
    executeRequestToResponse(CollectionApi().deleteCollection("", "", adminBearerToken, "")(collection))
  }
}
