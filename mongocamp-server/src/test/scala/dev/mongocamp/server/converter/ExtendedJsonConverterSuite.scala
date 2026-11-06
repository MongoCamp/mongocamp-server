package dev.mongocamp.server.converter

import dev.mongocamp.server.exception.MongoCampException
import java.util.UUID
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.bson.BsonBinary
import org.bson.BsonDecimal128
import org.bson.BsonDouble
import org.bson.BsonInt32
import org.bson.BsonMaxKey
import org.bson.BsonMinKey
import org.bson.BsonRegularExpression
import org.bson.BsonTimestamp

class ExtendedJsonConverterSuite extends munit.FunSuite {

  private def convert(key: String, value: Any): Any = ExtendedJsonConverter.convertWrapper(Map(key -> value)).getOrElse(fail(s"$key is not a wrapper"))

  private def refused(key: String, value: Any): Unit = {
    val exception = intercept[MongoCampException](convert(key, value))
    assertEquals(exception.statusCode.code, 400)
    assert(exception.getMessage.contains(key), exception.getMessage)
  }

  test("numbers") {
    assertEquals(convert("$numberInt", "123"), new BsonInt32(123))
    assertEquals(convert("$numberInt", "-2147483648"), new BsonInt32(Int.MinValue))
    assertEquals(convert("$numberInt", 7), new BsonInt32(7))
    assertEquals(convert("$numberDouble", "1.5"), new BsonDouble(1.5))
    assertEquals(convert("$numberDouble", "Infinity"), new BsonDouble(Double.PositiveInfinity))
    assertEquals(convert("$numberDouble", "-Infinity"), new BsonDouble(Double.NegativeInfinity))
    assert(convert("$numberDouble", "NaN").asInstanceOf[BsonDouble].getValue.isNaN)
    assertEquals(convert("$numberDecimal", "1.10"), new BsonDecimal128(Decimal128.parse("1.10")))
  }

  test("the id and the binary types") {
    assertEquals(convert("$oid", "507f1f77bcf86cd799439011"), new ObjectId("507f1f77bcf86cd799439011"))
    val binary = convert("$binary", Map("base64" -> "AQID", "subType" -> "00")).asInstanceOf[BsonBinary]
    assertEquals(binary.getType.toInt, 0)
    assertEquals(binary.getData.toList, List(1.toByte, 2.toByte, 3.toByte))
    assertEquals(convert("$binary", Map("base64" -> "AQID", "subType" -> "80")).asInstanceOf[BsonBinary].getType.toInt, -128)
    val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
    val bson = convert("$uuid", uuid.toString).asInstanceOf[BsonBinary]
    assertEquals(bson.getType.toInt, 4)
    assertEquals(bson.asUuid(), uuid)
  }

  test("timestamp, regular expression and the keys") {
    assertEquals(convert("$timestamp", Map("t" -> 1700000000L, "i" -> 5)), new BsonTimestamp(1700000000, 5))
    assertEquals(convert("$regularExpression", Map("pattern" -> "^a.*", "options" -> "i")), new BsonRegularExpression("^a.*", "i"))
    assertEquals(convert("$regularExpression", Map("pattern" -> "x")), new BsonRegularExpression("x", ""))
    assertEquals(convert("$minKey", 1), new BsonMinKey())
    assertEquals(convert("$maxKey", 1), new BsonMaxKey())
  }

  test("content that is not valid is a bad request, the key is in the message") {
    refused("$numberInt", "12.5")
    refused("$numberInt", "3000000000")
    refused("$numberInt", Map("a" -> 1))
    refused("$numberDouble", "one")
    refused("$numberDecimal", "abc")
    refused("$oid", "xyz")
    refused("$oid", 5)
    refused("$binary", "AQID")
    refused("$binary", Map("base64" -> "***", "subType" -> "00"))
    refused("$binary", Map("base64" -> "AQID", "subType" -> "zz"))
    refused("$binary", Map("base64" -> "AQID"))
    refused("$timestamp", Map("t" -> 1))
    refused("$timestamp", Map("t" -> -1, "i" -> 1))
    refused("$timestamp", Map("t" -> 1, "i" -> 1, "x" -> 1))
    refused("$regularExpression", Map("pattern" -> "x", "options" -> "q"))
    refused("$regularExpression", "x")
    refused("$uuid", "not-a-uuid")
    refused("$minKey", 2)
    refused("$maxKey", "max")
  }

  test("only a map with exactly one wrapper key is a wrapper") {
    assertEquals(ExtendedJsonConverter.convertWrapper(Map("a" -> 1)), None)
    assertEquals(ExtendedJsonConverter.convertWrapper(Map("$numberInt" -> "1", "other" -> 2)), None)
    assertEquals(ExtendedJsonConverter.convertWrapper(Map("$gt" -> 1)), None)
    assertEquals(ExtendedJsonConverter.convertWrapper(Map()), None)
  }

  test("in documents and filters: nested, in lists and in the id") {
    val converted = MongoCampBsonConverter.convertFields(
      Map(
        "_id"    -> Map("$numberInt" -> "7"),
        "n"      -> Map("$numberInt" -> "5"),
        "nested" -> Map("deep" -> Map("$numberDecimal" -> "2.50"), "list" -> List(Map("$numberInt" -> "1"), 2, Map("$oid" -> "507f1f77bcf86cd799439011"))),
        "filter" -> Map("$in" -> List(Map("$numberInt" -> "1"), Map("$numberInt" -> "2"))),
        "plain"  -> Map("a" -> 1, "$numberInt" -> "9")
      )
    )
    assertEquals(converted("_id"), new BsonInt32(7))
    assertEquals(converted("n"), new BsonInt32(5))
    val nested = converted("nested").asInstanceOf[Map[String, Any]]
    assertEquals(nested("deep"), new BsonDecimal128(Decimal128.parse("2.50")))
    assertEquals(nested("list").asInstanceOf[List[Any]], List(new BsonInt32(1), 2, new ObjectId("507f1f77bcf86cd799439011")))
    assertEquals(converted("filter").asInstanceOf[Map[String, Any]]("$in"), List(new BsonInt32(1), new BsonInt32(2)))
    // two keys: a document
    assertEquals(converted("plain"), Map("a" -> 1, "$numberInt" -> "9"))
  }
}
