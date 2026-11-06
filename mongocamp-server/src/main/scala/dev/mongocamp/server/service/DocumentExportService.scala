package dev.mongocamp.server.service

import dev.mongocamp.server.converter.CirceSchema
import dev.mongocamp.server.exception.MongoCampException
import io.circe.Json
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.Source

/** The documents of a collection as a stream of text in one of the formats of an export. Nothing is collected: every document is written when the database
  * delivers it, so the size of the collection does not matter for the memory of the server.
  */
object DocumentExportService extends CirceSchema {

  sealed trait ExportFormat {
    def name: String
    def extension: String
  }

  /** One JSON document per line. */
  case object Ndjson extends ExportFormat {
    val name      = "ndjson"
    val extension = "ndjson"
  }

  /** One JSON array with all documents. */
  case object JsonArray extends ExportFormat {
    val name      = "json"
    val extension = "json"
  }

  /** A line of values for every document, a header line with the names of the columns first. */
  case object Csv extends ExportFormat {
    val name      = "csv"
    val extension = "csv"
  }

  val Formats: List[ExportFormat] = List(Ndjson, JsonArray, Csv)

  def parseFormat(value: String): ExportFormat =
    Formats.find(_.name.equalsIgnoreCase(value.trim)).getOrElse(throw MongoCampException.badRequestException(s"format has to be ${Formats.map(_.name).mkString(", ")}, not `$value`"))

  def jsonText(document: Map[String, Any]): String = encodeAnyToJson(document).noSpaces

  /** The value of a cell: a text as it is, nothing for null, everything else (numbers, dates, objects, lists) as JSON. */
  def csvCell(value: Any): String = {
    val text = Option(value)
      .map(
        v =>
          encodeAnyToJson(v) match {
            case json if json.isString => json.asString.get
            case json if json.isNull   => ""
            case json                  => json.noSpaces
          }
      )
      .getOrElse("")
    if (text.exists(c => c == ',' || c == '"' || c == '\n' || c == '\r')) "\"" + text.replace("\"", "\"\"") + "\"" else text
  }

  def csvHeader(columns: List[String]): String = columns.map(csvCell).mkString(",") + "\r\n"

  def csvRow(columns: List[String], document: Map[String, Any]): String = columns.map(column => csvCell(document.getOrElse(column, null))).mkString(",") + "\r\n"

  /** The text of the export. For csv `columns` are the columns, for the other formats they are not used. */
  def render(format: ExportFormat, documents: Source[Map[String, Any], NotUsed], columns: List[String]): Source[String, NotUsed] =
    format match {
      case Ndjson    => documents.map(document => jsonText(document) + "\n")
      case JsonArray => documents.map(jsonText).intersperse("[", ",", "]")
      case Csv       => Source.single(csvHeader(columns)).concat(documents.map(csvRow(columns, _)))
    }
}
