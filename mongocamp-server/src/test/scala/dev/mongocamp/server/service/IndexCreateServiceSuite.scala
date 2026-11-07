package dev.mongocamp.server.service

import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.index.IndexKey
import dev.mongocamp.server.model.index.IndexOptionsRequest
import org.bson.BsonInt32
import org.bson.BsonString

class IndexCreateServiceSuite extends munit.FunSuite {

  private def refused(body: => Any): MongoCampException = {
    val exception = intercept[MongoCampException](body)
    assertEquals(exception.statusCode.code, 400)
    assertEquals(exception.errorCode, ErrorCodes.invalidIndexKey)
    exception
  }

  private def options(f: IndexOptionsRequest => IndexOptionsRequest) =
    IndexCreateService.indexOptions(Some(f(IndexOptionsRequest(None, None, None, None, None, None, None, None))))

  test("the keys of the older request are 1 or -1") {
    val document = IndexCreateService.keysDocument(Some(Map("a" -> 1, "b" -> -1)), None)
    assertEquals(document.get("a"), new BsonInt32(1))
    assertEquals(document.get("b"), new BsonInt32(-1))
  }

  test("any other direction is refused, the database would create an index z_5") {
    assert(refused(IndexCreateService.keysDocument(Some(Map("z" -> 5)), None)).getMessage.contains("z"))
    refused(IndexCreateService.keysDocument(Some(Map("z" -> 0)), None))
  }

  test("ordered keys keep the order and allow the types of keys") {
    val document = IndexCreateService.keysDocument(
      None,
      Some(List(IndexKey("c", "1"), IndexKey("b", "-1"), IndexKey("t1", "text"), IndexKey("t2", "text"), IndexKey("loc", "2dsphere"), IndexKey("h", "hashed")))
    )
    assertEquals(document.keySet().toArray.toList, List("c", "b", "t1", "t2", "loc", "h"))
    assertEquals(document.get("c"), new BsonInt32(1))
    assertEquals(document.get("b"), new BsonInt32(-1))
    assertEquals(document.get("t1"), new BsonString("text"))
    assertEquals(document.get("loc"), new BsonString("2dsphere"))
  }

  test("an unknown direction is refused") {
    refused(IndexCreateService.keysDocument(None, Some(List(IndexKey("a", "banana")))))
    refused(IndexCreateService.keysDocument(None, Some(List(IndexKey("a", "5")))))
  }

  test("no key, both kinds of keys and a field twice are refused") {
    refused(IndexCreateService.keysDocument(None, None))
    refused(IndexCreateService.keysDocument(Some(Map()), Some(List())))
    refused(IndexCreateService.keysDocument(Some(Map("a" -> 1)), Some(List(IndexKey("b", "1")))))
    refused(IndexCreateService.keysDocument(None, Some(List(IndexKey("a", "1"), IndexKey("a", "-1")))))
    refused(IndexCreateService.keysDocument(None, Some(List(IndexKey(" ", "1")))))
  }

  test("empty lists count as not sent, so the other kind of keys can be used") {
    assertEquals(IndexCreateService.keysDocument(Some(Map()), Some(List(IndexKey("a", "1")))).keySet().toArray.toList, List("a"))
    assertEquals(IndexCreateService.keysDocument(Some(Map("a" -> 1)), Some(List())).keySet().toArray.toList, List("a"))
  }

  test("a collation is checked") {
    val collation = IndexCreateService.collation(Map("locale" -> "de", "strength" -> 2, "caseLevel" -> true, "numericOrdering" -> true, "caseFirst" -> "upper"))
    assertEquals(collation.getLocale, "de")
    assertEquals(collation.getStrength.getIntRepresentation, 2)
    assertEquals(collation.getCaseLevel.booleanValue(), true)
    refused(IndexCreateService.collation(Map("strength" -> 2)))
    refused(IndexCreateService.collation(Map("locale" -> "de", "strength" -> 9)))
    refused(IndexCreateService.collation(Map("locale" -> "de", "caseFirst" -> "sideways")))
    refused(IndexCreateService.collation(Map("locale" -> "de", "unknown" -> 1)))
    refused(IndexCreateService.collation(Map("locale" -> "de", "backwards" -> "yes")))
  }

  test("the options are converted") {
    val converted =
      options(_.copy(sparse = Some(true), hidden = Some(true), weights = Some(Map("a" -> 5)), partialFilterExpression = Some(Map("a" -> Map("$gt" -> 1)))))
    assertEquals(converted.isSparse, true)
    assertEquals(converted.isHidden, true)
    assertEquals(converted.getWeights.toBsonDocument().getInt32("a").getValue, 5)
    assertEquals(converted.getPartialFilterExpression.toBsonDocument().containsKey("a"), true)
  }

  // the documents are the ones that the database returns for listIndexes, they are created again on another collection
  private def fromDatabase(json: String) = org.bson.BsonDocument.parse(json)

  test("the keys of an index of the database: a text index is created again with its fields") {
    val text = fromDatabase("""{"v": 2, "key": {"category": 1, "_fts": "text", "_ftsx": 1}, "name": "t", "weights": {"title": 5, "body": 1}}""")
    val keys = IndexCreateService.keysFromIndexDocument(text)
    assertEquals(keys.keySet().toArray.toList, List("category", "title", "body"))
    assertEquals(keys.get("title"), new BsonString("text"))
    assertEquals(IndexCreateService.keysFromIndexDocument(fromDatabase("""{"key": {"b": -1, "a": 1}, "name": "x"}""")).keySet().toArray.toList, List("b", "a"))
  }

  test("the options of an index of the database") {
    val options = IndexCreateService.optionsFromIndexDocument(
      fromDatabase("""{"v": 2, "key": {"email": 1}, "name": "mail", "unique": true, "sparse": true, "hidden": true, "expireAfterSeconds": 3600,
          |"partialFilterExpression": {"active": true}, "collation": {"locale": "de", "caseLevel": false, "caseFirst": "off", "strength": 2, "numericOrdering": false,
          |"alternate": "non-ignorable", "maxVariable": "punct", "normalization": false, "backwards": false, "version": "57.1"}}""".stripMargin)
    )
    assertEquals(options.getName, "mail")
    assertEquals((options.isUnique, options.isSparse, options.isHidden), (true, true, true))
    assertEquals(options.getExpireAfter(java.util.concurrent.TimeUnit.SECONDS).longValue(), 3600L)
    assertEquals(options.getCollation.getLocale, "de")
    assertEquals(options.getPartialFilterExpression.toBsonDocument().getBoolean("active").getValue, true)
  }
}
