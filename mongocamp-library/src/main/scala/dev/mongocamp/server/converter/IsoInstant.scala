package dev.mongocamp.server.converter

import java.time.DateTimeException
import java.time.OffsetDateTime
import org.joda.time.DateTime

/** A string is a date if it is a full ISO-8601 instant: date, time with seconds, an optional fraction of a second, and `Z` or an offset. These are the forms
  * that `JSON.stringify(new Date())` (with milliseconds), `Instant.toString` (without zero milliseconds) and most clients write. Strings without a time or a
  * zone (`2024-06-15`, `2024-06-15T12:00:00`) are not dates, they are stored as they are.
  */
object IsoInstant {
  private val instantPattern = """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{1,9})?(Z|[+-]\d{2}:\d{2})""".r

  def parse(string: String): Option[DateTime] = {
    if (string.length >= 20 && string.length <= 35 && instantPattern.matches(string)) {
      try
        Some(new DateTime(OffsetDateTime.parse(string).toInstant.toEpochMilli))
      catch {
        case _: DateTimeException => None
      }
    }
    else {
      None
    }
  }
}
