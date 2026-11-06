package dev.mongocamp.server.tests

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.test.client.api.IndexApi
import dev.mongocamp.server.test.client.api.IndexCreateApi
import dev.mongocamp.server.test.client.api.IndexDetailsApi
import dev.mongocamp.server.test.client.model.IndexCreateRequest
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import io.circe.Json
import io.circe.parser.parse
import org.mongodb.scala.bson.collection.immutable.Document

// the request to create an index with ordered keys, the options of the newer versions and the check of the directions
class IndexCreateSuite extends MongoCampBaseServerSuite {

  private lazy val createApi  = IndexCreateApi()
  private lazy val detailsApi = IndexDetailsApi()
  private lazy val indexApi   = IndexApi()

  private val collection = "indexCreateTest"

  private def dao = MongoDatabase.databaseProvider.dao(collection)

  private def create(body: String, token: String = adminBearerToken, name: String = collection) =
    executeRequest(createApi.createIndex("", "", token, "")(name, parse(body).getOrElse(Json.Null)))

  private def indices =
    executeRequestToResponse(detailsApi.indexDetails("", "", adminBearerToken, "")(collection)).map(i => (i.name, i)).toMap

  test("prepare") {
    dao.insertOne(Document("a" -> 1, "category" -> "x", "t1" -> "a", "t2" -> "b")).result()
  }

  test("ordered keys are created in their order") {
    val response = create("""{"orderedKeys": [{"field": "c", "direction": "1"}, {"field": "b", "direction": "-1"}, {"field": "a", "direction": "1"}, {"field": "z", "direction": "1"}, {"field": "y", "direction": "1"}, {"field": "x", "direction": "-1"}], "indexOptionsRequest": {"name": "six"}}""")
    assertEquals(response.code.code, 200, response.body.fold(_.getMessage, _ => ""))
    assertEquals(indices("six").keys.map(k => (k.field, k.direction)), Seq(("c", "1"), ("b", "-1"), ("a", "1"), ("z", "1"), ("y", "1"), ("x", "-1")))
  }

  test("a compound text index with weights and a default language") {
    val response = create(
      """{"orderedKeys": [{"field": "category", "direction": "1"}, {"field": "t1", "direction": "text"}, {"field": "t2", "direction": "text"}], "indexOptionsRequest": {"name": "texts", "weights": {"t1": 5, "t2": 1}, "defaultLanguage": "german"}}"""
    )
    assertEquals(response.code.code, 200, response.body.fold(_.getMessage, _ => ""))
    val text = indices("texts")
    assertEquals(text.keys.map(k => (k.field, k.direction)), Seq(("category", "1"), ("t1", "text"), ("t2", "text")))
    assertEquals(text.weights.map(_.map(e => (e._1, e._2.toString.toDouble.toInt))), Some(Map("t1" -> 5, "t2" -> 1)))
    assertEquals(text.defaultLanguage, Some("german"))
  }

  test("the options sparse, hidden, partial filter and collation") {
    val sparse = create("""{"orderedKeys": [{"field": "a", "direction": "1"}], "indexOptionsRequest": {"name": "withOptions", "sparse": true, "hidden": true, "collation": {"locale": "de", "strength": 2}}}""")
    assertEquals(sparse.code.code, 200, sparse.body.fold(_.getMessage, _ => ""))
    val partial = create("""{"orderedKeys": [{"field": "b", "direction": "1"}], "indexOptionsRequest": {"name": "partialIndex", "partialFilterExpression": {"b": {"$gt": 1}}}}""")
    assertEquals(partial.code.code, 200, partial.body.fold(_.getMessage, _ => ""))
    val all = indices
    assertEquals((all("withOptions").sparse, all("withOptions").hidden), (true, true))
    assertEquals(all("withOptions").collation.flatMap(_.get("locale")), Some("de"))
    assertEquals(all("partialIndex").partialFilterExpression.map(_.keySet), Some(Set("b")))
  }

  test("a direction that is not 1 or -1 is a bad request, in both forms of the keys") {
    val legacy = create("""{"keys": {"z": 5}}""")
    assertEquals(legacy.code.code, 400)
    assertEquals(legacy.header("x-error-code"), Some(ErrorCodes.invalidIndexKey.toString))
    assertEquals(indices.keySet.exists(_.startsWith("z_5")), false)
    val ordered = create("""{"orderedKeys": [{"field": "z", "direction": "banana"}]}""")
    assertEquals(ordered.code.code, 400)
    assertEquals(ordered.header("x-error-code"), Some(ErrorCodes.invalidIndexKey.toString))
  }

  test("no keys and both kinds of keys are a bad request") {
    assertEquals(create("""{"keys": {}}""").code.code, 400)
    assertEquals(create("""{"keys": {"a": 1}, "orderedKeys": [{"field": "b", "direction": "1"}]}""").code.code, 400)
    assertEquals(create("""{"orderedKeys": [{"field": "a", "direction": "1"}, {"field": "a", "direction": "-1"}]}""").code.code, 400)
  }

  test("an invalid collation is a bad request") {
    val response = create("""{"orderedKeys": [{"field": "a", "direction": "1"}], "indexOptionsRequest": {"name": "badCollation", "collation": {"strength": 2}}}""")
    assertEquals(response.code.code, 400)
    assertEquals(indices.contains("badCollation"), false)
  }

  test("the request of the older versions still works") {
    val response = executeRequestToResponse(indexApi.createIndex("", "", adminBearerToken, "")(collection, IndexCreateRequest(Map("t2" -> -1), None)))
    assertEquals(response.name, "t2_-1")
  }

  test("an index can only be created with the administrate grant") {
    val code = create("""{"orderedKeys": [{"field": "a", "direction": "1"}]}""", token = testUserBearerToken, name = "accounts").code.code
    assert(Set(401, 403).contains(code), s"unexpected status $code")
  }

  test("cleanup") {
    dao.drop().result()
  }
}
