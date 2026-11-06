package dev.mongocamp.server.tests

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.test.client.api.IndexDetailsApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.IndexOptions
import org.mongodb.scala.model.Indexes

// the indices with the order of their keys and all options, created directly in the database
class IndexDetailsSuite extends MongoCampBaseServerSuite {

  private lazy val detailsApi = IndexDetailsApi()

  private val collection = "indexDetailsTest"

  private def dao = MongoDatabase.databaseProvider.dao(collection)

  private def details(token: String = adminBearerToken, name: String = collection) = executeRequest(detailsApi.indexDetails("", "", token, "")(name))

  private def indices = {
    val response = details()
    assertEquals(response.code.code, 200, response.body.fold(_.getMessage, _ => ""))
    response.body.getOrElse(Nil).map(i => (i.name, i)).toMap
  }

  test("prepare: the indices are created in the database") {
    dao.insertOne(Document("a" -> 1, "b" -> 2, "c" -> 3, "title" -> "t", "body" -> "b")).result()
    dao.createIndex(Indexes.compoundIndex(Indexes.ascending("c"), Indexes.descending("b")), IndexOptions().name("compound")).result()
    dao.createIndex(Document("c" -> 1, "a" -> 1, "z" -> 1, "y" -> 1, "x" -> 1, "w" -> -1), IndexOptions().name("six")).result()
    // sparse and partialFilterExpression cannot be combined in one index
    dao.createIndex(Document("a" -> 1), IndexOptions().name("partial").unique(true).partialFilterExpression(Document("b" -> Document("$gt" -> 1)))).result()
    dao.createIndex(Document("b" -> 1), IndexOptions().name("sparseIndex").sparse(true)).result()
    dao.createIndex(Document("title" -> "text", "body" -> "text"), IndexOptions().name("texts").defaultLanguage("german")).result()
  }

  test("the keys of a compound index are in the order of the index") {
    val all = indices
    assertEquals(all("compound").keys.map(k => (k.field, k.direction)), Seq(("c", "1"), ("b", "-1")))
    assertEquals(all("six").keys.map(_.field), Seq("c", "a", "z", "y", "x", "w"))
    assertEquals(all("_id_").keys.map(k => (k.field, k.direction)), Seq(("_id", "1")))
  }

  test("the options of an index") {
    val all     = indices
    val partial = all("partial")
    assertEquals((partial.unique, partial.sparse), (true, false))
    assertEquals(partial.partialFilterExpression.map(_.keySet), Some(Set("b")))
    assertEquals((all("sparseIndex").sparse, all("sparseIndex").unique), (true, false))
    assertEquals(all("compound").partialFilterExpression, None)
  }

  test("a text index lists its fields, the weights and the language") {
    val text = indices("texts")
    assertEquals(text.keys.map(k => (k.field, k.direction)).toSet, Set(("title", "text"), ("body", "text")))
    assertEquals(text.weights.map(_.keySet), Some(Set("title", "body")))
    assertEquals(text.defaultLanguage, Some("german"))
    assertEquals(text.keys.exists(_.field.startsWith("_fts")), false)
  }

  test("the listing needs the administrate grant") {
    val denied = details(token = testUserBearerToken, name = "accounts").code.code
    assert(Set(401, 403).contains(denied), s"unexpected status $denied")
    assertEquals(details(token = testUserBearerToken, name = "test").code.code, 200)
  }

  test("cleanup") {
    dao.drop().result()
  }
}
