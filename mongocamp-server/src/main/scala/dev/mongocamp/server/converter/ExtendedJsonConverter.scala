package dev.mongocamp.server.converter

import dev.mongocamp.server.exception.MongoCampException
import java.util.Base64
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
import org.bson.UuidRepresentation
import scala.util.Try

/** The MongoDB Extended JSON wrappers in a request body, that give a value its BSON type: `{"$numberInt": "5"}`, `{"$numberDouble": "1.5"}`,
  * `{"$numberDecimal": "1.10"}`, `{"$oid": "..."}`, `{"$binary": {"base64": "...", "subType": "00"}}`, `{"$timestamp": {"t": 1, "i": 2}}`,
  * `{"$regularExpression": {"pattern": "a.*", "options": "i"}}`, `{"$uuid": "..."}`, `{"$minKey": 1}` and `{"$maxKey": 1}`. A map with exactly one of these keys
  * is a wrapper, every other map is a document. Content that is not valid is a bad request (the value would be a sub document with the wrapper as key
  * otherwise). `$date` and `$numberLong` are converted by [[MongoCampBsonConverter]], they give a `Date` and a `Long`.
  */
object ExtendedJsonConverter {

  val WrapperKeys: Set[String] =
    Set("$numberInt", "$numberDouble", "$numberDecimal", "$oid", "$binary", "$timestamp", "$regularExpression", "$uuid", "$minKey", "$maxKey")

  private def invalid(key: String, expected: String, got: Any) =
    MongoCampException.badRequestException(s"""the value of "$key" must be $expected, got ${String.valueOf(got).take(60)}""")

  /** The converted value for a wrapper, None for a map that is not one. */
  def convertWrapper(map: Map[String, Any]): Option[Any] =
    if (map.size == 1 && WrapperKeys.contains(map.head._1)) Some(convert(map.head._1, map.head._2)) else None

  private def convert(key: String, value: Any): Any =
    key match {
      case "$numberInt"          => new BsonInt32(numberInt(value))
      case "$numberDouble"       => new BsonDouble(numberDouble(value))
      case "$numberDecimal"      => new BsonDecimal128(numberDecimal(value))
      case "$oid"                => objectId(value)
      case "$binary"             => binary(value)
      case "$timestamp"          => timestamp(value)
      case "$regularExpression"  => regularExpression(value)
      case "$uuid"               => uuid(value)
      case "$minKey"             => minMaxKey(key, value, new BsonMinKey())
      case "$maxKey"             => minMaxKey(key, value, new BsonMaxKey())
    }

  private def numberInt(value: Any): Int =
    value match {
      case s: String => s.trim.toIntOption.getOrElse(throw invalid("$numberInt", "a number with up to 32 bit", s))
      case i: Int    => i
      case l: Long   => if (l.isValidInt) l.toInt else throw invalid("$numberInt", "a number with up to 32 bit", l)
      case other     => throw invalid("$numberInt", "a string with a number with up to 32 bit", other)
    }

  private def numberDouble(value: Any): Double =
    value match {
      case s: String =>
        s.trim match {
          case "NaN"       => Double.NaN
          case "Infinity"  => Double.PositiveInfinity
          case "-Infinity" => Double.NegativeInfinity
          case text        => text.toDoubleOption.getOrElse(throw invalid("$numberDouble", "a decimal number, NaN, Infinity or -Infinity", s))
        }
      case n: Number => n.doubleValue()
      case other     => throw invalid("$numberDouble", "a string with a decimal number", other)
    }

  private def numberDecimal(value: Any): Decimal128 =
    value match {
      case s: String => Try(Decimal128.parse(s.trim)).getOrElse(throw invalid("$numberDecimal", "a decimal number with up to 34 digits", s))
      case other     => throw invalid("$numberDecimal", "a string with a decimal number", other)
    }

  private def objectId(value: Any): ObjectId =
    value match {
      case s: String if ObjectId.isValid(s) => new ObjectId(s)
      case other                            => throw invalid("$oid", "a string with 24 hexadecimal characters", other)
    }

  private def fields(key: String, value: Any, expected: String): Map[String, Any] =
    value match {
      case map: Map[_, _] => map.asInstanceOf[Map[String, Any]]
      case other          => throw invalid(key, expected, other)
    }

  private def binary(value: Any): BsonBinary = {
    val expected = """an object with "base64" and "subType"""""
    val map      = fields("$binary", value, expected)
    val base64   = map.get("base64").collect { case s: String => s }.getOrElse(throw invalid("$binary", expected, value))
    val subType  = map.get("subType").collect { case s: String => s }.getOrElse(throw invalid("$binary", expected, value))
    val bytes    = Try(Base64.getDecoder.decode(base64)).getOrElse(throw invalid("$binary", "valid base64 in base64", base64))
    val subTypeByte =
      Try(Integer.parseInt(subType, 16)).toOption.filter(code => code >= 0 && code <= 255).getOrElse(throw invalid("$binary", "a subType of one byte as hexadecimal text", subType))
    new BsonBinary(subTypeByte.toByte, bytes)
  }

  private def timestamp(value: Any): BsonTimestamp = {
    val expected = """an object with the numbers "t" and "i"""""
    val map      = fields("$timestamp", value, expected)
    def unsigned(name: String): Int =
      map.get(name) match {
        case Some(n: Number) if n.longValue() >= 0 && n.longValue() <= 4294967295L => n.longValue().toInt
        case _                                                                      => throw invalid("$timestamp", s"""a number from 0 to 4294967295 in "$name"""", value)
      }
    if (map.size != 2) throw invalid("$timestamp", expected, value)
    new BsonTimestamp(unsigned("t"), unsigned("i"))
  }

  private def regularExpression(value: Any): BsonRegularExpression = {
    val expected = """an object with "pattern" and "options"""""
    val map      = fields("$regularExpression", value, expected)
    val pattern  = map.get("pattern").collect { case s: String => s }.getOrElse(throw invalid("$regularExpression", expected, value))
    val options  = map.get("options").collect { case s: String => s }.getOrElse("")
    if (!options.forall("imsxlu".contains(_))) throw invalid("$regularExpression", "options of the letters i m s x l u", options)
    new BsonRegularExpression(pattern, options)
  }

  private def uuid(value: Any): BsonBinary =
    value match {
      case s: String => new BsonBinary(Try(UUID.fromString(s)).getOrElse(throw invalid("$uuid", "a UUID like 123e4567-e89b-12d3-a456-426614174000", s)), UuidRepresentation.STANDARD)
      case other     => throw invalid("$uuid", "a string with a UUID", other)
    }

  private def minMaxKey[T](key: String, value: Any, result: T): T =
    value match {
      case n: Number if n.intValue() == 1 && n.doubleValue() == 1.0 => result
      case s: String if s == "1"                                  => result
      case other                                                  => throw invalid(key, "1", other)
    }
}
