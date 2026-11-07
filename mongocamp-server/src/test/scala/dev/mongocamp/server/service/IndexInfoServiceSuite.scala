package dev.mongocamp.server.service

import dev.mongocamp.server.model.index.IndexKey
import org.bson.BsonDocument

// the documents are the ones that the database returns for listIndexes
class IndexInfoServiceSuite extends munit.FunSuite {

  private def info(json: String) = IndexInfoService.indexInfo(BsonDocument.parse(json))

  test("the keys of a compound index are in the order of the index") {
    val index = info("""{"v": 2, "key": {"c": 1, "b": -1, "a": 1, "z": 1, "y": 1, "x": -1}, "name": "c_1_b_-1_a_1_z_1_y_1_x_-1"}""")
    assertEquals(
      index.keys.map(
        k => (k.field, k.direction)
      ),
      List(("c", "1"), ("b", "-1"), ("a", "1"), ("z", "1"), ("y", "1"), ("x", "-1"))
    )
    assertEquals(index.version, 2)
    assertEquals((index.unique, index.sparse, index.hidden), (false, false, false))
  }

  test("a name that does not say the order does not matter") {
    val index = info("""{"v": 2, "key": {"b": 1, "a": 1}, "name": "mine"}""")
    assertEquals(index.name, "mine")
    assertEquals(index.keys.map(_.field), List("b", "a"))
  }

  test("a text index lists its fields and the weights, not the internal keys") {
    val index = info(
      """{"v": 2, "key": {"_fts": "text", "_ftsx": 1}, "name": "title_text_body_text", "weights": {"title": 10, "body": 1}, "default_language": "german", "language_override": "language", "textIndexVersion": 3}"""
    )
    assertEquals(index.keys, List(IndexKey("title", "text"), IndexKey("body", "text")))
    assertEquals(index.weights, Some(Map[String, Any]("title" -> 10, "body" -> 1)))
    assertEquals(index.defaultLanguage, Some("german"))
  }

  test("a compound text index keeps the fields before and after the text fields") {
    val index = info("""{"v": 2, "key": {"category": 1, "_fts": "text", "_ftsx": 1, "created": -1}, "name": "c", "weights": {"t1": 1, "t2": 1}}""")
    assertEquals(
      index.keys.map(
        k => (k.field, k.direction)
      ),
      List(("category", "1"), ("t1", "text"), ("t2", "text"), ("created", "-1"))
    )
  }

  test("the options of an index") {
    val index = info("""{"v": 2, "key": {"email": 1}, "name": "email_1", "unique": true, "sparse": true, "hidden": true, "expireAfterSeconds": 3600,
        |"partialFilterExpression": {"active": true, "age": {"$gt": 18}}, "collation": {"locale": "de", "strength": 2}}""".stripMargin)
    assertEquals((index.unique, index.sparse, index.hidden), (true, true, true))
    assertEquals(index.expireAfterSeconds, Some(3600L))
    assertEquals(index.partialFilterExpression.map(_.keySet), Some(Set("active", "age")))
    assertEquals(index.partialFilterExpression.flatMap(_.get("active")), Some(true))
    assertEquals(index.collation.flatMap(_.get("locale")), Some("de"))
  }

  test("the types of a special key") {
    val index = info("""{"v": 2, "key": {"loc": "2dsphere", "name": "hashed", "legacy": "2d"}, "name": "special"}""")
    assertEquals(
      index.keys.map(
        k => (k.field, k.direction)
      ),
      List(("loc", "2dsphere"), ("name", "hashed"), ("legacy", "2d"))
    )
  }

  test("an index without options has none") {
    val index = info("""{"v": 2, "key": {"_id": 1}, "name": "_id_"}""")
    assertEquals(
      (index.partialFilterExpression, index.collation, index.weights, index.defaultLanguage, index.expireAfterSeconds),
      (None, None, None, None, None)
    )
  }
}
