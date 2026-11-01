package dev.mongocamp.server.converter

import dev.mongocamp.server.model.MongoCampConfiguration
import io.circe.Decoder
import io.circe.Decoder.Result
import io.circe.Encoder
import io.circe.HCursor
import io.circe.Json
import java.time.DateTimeException
import java.time.OffsetDateTime
import java.util.Date
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.mongodb.scala.Document

// Encoder[Any] matches every type, so it has to rank below the more specific instances of CirceSchema (Scala 3 reports them as ambiguous otherwise)
trait LowPriorityCirceSchema { self: CirceSchema =>

  implicit val AnyFormat: Encoder[Any] & Decoder[Any] = new Encoder[Any] with Decoder[Any] {
    override def apply(a: Any): Json = encodeAnyToJson(a)

    override def apply(c: HCursor): Result[Any] = {
      Decoder.decodeJson
        .map(
          a => decodeFromJson(a)
        )
        .apply(c)
    }
  }

}

trait CirceSchema extends LowPriorityCirceSchema {

  implicit val DateFormat: Encoder[Date] & Decoder[Date] = new Encoder[Date] with Decoder[Date] {
    override def apply(a: Date): Json = Encoder.encodeString.apply(a.toInstant.toString)

    override def apply(c: HCursor): Result[Date] = Decoder.decodeString
      .map(
        s => new DateTime(s).toDate
      )
      .apply(c)
  }

  implicit val DateTimeFormat: Encoder[DateTime] & Decoder[DateTime] = new Encoder[DateTime] with Decoder[DateTime] {
    override def apply(a: DateTime): Json = Encoder.encodeString.apply(a.toInstant.toString)

    override def apply(c: HCursor): Result[DateTime] = Decoder.decodeString
      .map(
        s => new DateTime(s)
      )
      .apply(c)
  }

  implicit val ObjectIdFormat: Encoder[ObjectId] & Decoder[ObjectId] = new Encoder[ObjectId] with Decoder[ObjectId] {
    override def apply(a: ObjectId): Json = Encoder.encodeString.apply(a.toHexString)

    override def apply(c: HCursor): Result[ObjectId] = Decoder.decodeString
      .map(
        s => new ObjectId(s)
      )
      .apply(c)
  }

  implicit val MapStringAnyFormat: Encoder[Map[String, Any]] & Decoder[Map[String, Any]] = new Encoder[Map[String, Any]] with Decoder[Map[String, Any]] {
    override def apply(a: Map[String, Any]): Json = encodeMapStringAny(a)

    override def apply(c: HCursor): Result[Map[String, Any]] = Decoder.decodeMap[String, Any].apply(c)
  }

  implicit val ConfigFormat: Encoder[MongoCampConfiguration] = new Encoder[MongoCampConfiguration] {
    override def apply(a: MongoCampConfiguration): Json = encodeAnyToJson(a)
  }

  def encodeMapStringAny(a: Map[String, Any]): Json = {
    Json.obj(
      a.keySet
        .map(
          key => (key, encodeAnyToJson(a(key)))
        )
        .toList: _*
    )
  }

  // A string is a date if it is a full ISO-8601 instant: date, time with seconds, optional fraction of a second, and `Z` or an offset. These are the forms that
  // JSON.stringify(new Date()) (with milliseconds), Instant.toString (without zero milliseconds) and most clients write. Strings without a time or a zone
  // (`2024-06-15`, `2024-06-15T12:00:00`) are not dates, they are stored as they are.
  private val instantPattern = """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{1,9})?(Z|[+-]\d{2}:\d{2})""".r

  private def parseInstant(string: String): Option[DateTime] = {
    if (string.length >= 20 && string.length <= 35 && instantPattern.matches(string)) {
      try {
        Some(new DateTime(OffsetDateTime.parse(string).toInstant.toEpochMilli))
      }
      catch {
        case _: DateTimeException => None
      }
    }
    else {
      None
    }
  }

  def decodeFromJson(json: Json): Any = {
    json match {
      case a if a.isNumber =>
        val value = a.asNumber.get
        val long  = value.toLong
        if (long.isDefined) {
          long.get
        }
        else {
          value.toDouble
        }
      case a if a.isString =>
        val string = a.asString.get
        parseInstant(string).getOrElse(string)
      case a if a.isBoolean => a.asBoolean.getOrElse(false)
      case a if a.isArray =>
        a.asArray.get.toList.map(
          e => decodeFromJson(e)
        )
      case a if a.isObject =>
        a.asObject.get.toMap.map(
          e => (e._1, decodeFromJson(e._2))
        )
      case a if a.isNull => null
      case _             => null
    }
  }

  def encodeAnyToJson(a: Any, deepth: Int = 0): Json = {
    a match {
      case s: String         => Json.fromString(s)
      case b: Boolean        => Json.fromBoolean(b)
      case l: Long           => Json.fromLong(l)
      case i: Int            => Json.fromInt(i)
      case bi: BigInt        => Json.fromBigInt(bi)
      case bd: BigDecimal    => Json.fromBigDecimal(bd)
      case d: Double         => Json.fromDoubleOrNull(d)
      case f: Float          => Json.fromFloatOrNull(f)
      case d: Date           => Encoder.encodeString.apply(d.toInstant.toString)
      case d: DateTime       => Encoder.encodeString.apply(d.toInstant.toString)
      case o: ObjectId       => Encoder.encodeString.apply(o.toHexString)
      case m: Map[String, _] => encodeMapStringAny(m)
      case seq: Seq[_] =>
        Json.arr(
          seq.map(
            e => encodeAnyToJson(e, deepth)
          ): _*
        )
      case set: Set[_] =>
        Json.arr(
          set
            .map(
              e => encodeAnyToJson(e, deepth)
            )
            .toList: _*
        )
      case product: Product =>
        val productElementNames = product.productElementNames.toList
        val fieldMap = productElementNames
          .map(
            key => {
              val index = productElementNames.indexOf(key)
              (key, product.productElement(index))
            }
          )
          .toMap
        encodeAnyToJson(fieldMap)
      case r: Document => encodeAnyToJson(r.toMap)
      case any: Any =>
        if (deepth < 256) {
          encodeAnyToJson(any, deepth + 1)
        }
        else {
          Json.Null
        }
      case _ =>
        Json.Null
    }
  }

}
