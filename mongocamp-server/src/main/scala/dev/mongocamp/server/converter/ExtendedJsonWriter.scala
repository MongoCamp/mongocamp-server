package dev.mongocamp.server.converter

import dev.mongocamp.server.exception.MongoCampException
import io.circe.parser.parse
import io.circe.Json
import org.bson.json.JsonMode
import org.bson.json.JsonWriterSettings
import org.bson.BsonDocument

/** The documents of a response with the types of the values, as MongoDB Extended JSON: the query parameter `extendedJson`.
  *   - `relaxed`: numbers are numbers, the other types are wrappers (`{"$oid": ...}`, `{"$date": "2024-06-15T12:00:00Z"}`, `{"$numberDecimal": "10.25"}`)
  *   - `canonical`: every number has a wrapper too (`{"$numberInt": "5"}`, `{"$numberLong": "5"}`, `{"$numberDouble": "1.5"}`), so nothing is lost for a 64 bit
  *     integer or a double that is a whole number
  *
  * A text that looks like an ISO-8601 instant is `{"$string": "..."}` in both modes (a plain one is read as a date in a request body).
  * The wrappers are the ones that are read in a request body ([[ExtendedJsonConverter]], `$date`, `$numberLong`), what is read in canonical mode can be sent as it
  * is. Without the parameter the documents are written as before.
  */
object ExtendedJsonWriter {

  sealed trait Mode {
    def name: String
    def settings: JsonWriterSettings
  }

  case object Relaxed extends Mode {
    val name     = "relaxed"
    val settings = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build()
  }

  case object Canonical extends Mode {
    val name     = "canonical"
    val settings = JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED).build()
  }

  val Modes: List[Mode] = List(Relaxed, Canonical)

  /** The mode of the query parameter, None without the parameter. Another value is a bad request. */
  def parseMode(value: Option[String]): Option[Mode] =
    value.map(_.trim).filter(_.nonEmpty).map(
      text =>
        Modes
          .find(_.name.equalsIgnoreCase(text))
          .getOrElse(throw MongoCampException.badRequestException(s"extendedJson has to be ${Modes.map(_.name).mkString(" or ")}, not `$text`"))
    )

  def toMap(document: BsonDocument, mode: Mode): Map[String, Any] =
    parse(document.toJson(mode.settings)).fold(error => throw error, json => json.asObject.map(_.toMap.map(entry => (entry._1, toValue(entry._2)))).getOrElse(Map()))

  // A text that looks like a date is {"$string": ...}: a plain ISO string in a request body is read as a date, so what is read here would be a date when it is
  // sent back. Dates are {"$date": ...}, so the two cannot be mixed up.
  private def toValue(json: Json): Any =
    json.fold(
      null,
      boolean => boolean,
      number => number.toLong.getOrElse(number.toDouble),
      string => if (IsoInstant.parse(string).isDefined) Map(CirceSchema.TextMarker -> string) else string,
      array => array.toList.map(toValue),
      obj =>
        // the ISO string of {"$date": "..."} is the date itself, it is not a text
        if (obj.size == 1 && obj.contains("$date") && obj("$date").exists(_.isString)) obj.toMap.map(entry => (entry._1, entry._2.asString.get))
        else obj.toMap.map(entry => (entry._1, toValue(entry._2)))
    )
}
