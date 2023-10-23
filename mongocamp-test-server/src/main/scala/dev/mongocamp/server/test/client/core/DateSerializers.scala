package dev.mongocamp.server.test.client.core

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

trait DateSerializers {
  import io.circe.{ Decoder, Encoder }
  implicit val dateTimeDecoder: Decoder[DateTime] = Decoder.decodeString.map(ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(_))
  implicit val dateTimeEncoder: Encoder[DateTime] = Encoder.encodeString.contramap(ISODateTimeFormat.dateTime().print(_))

  implicit val localDateDecoder: Decoder[org.joda.time.LocalDate] =
    Decoder.decodeString.map(org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd").parseLocalDate(_))
  implicit val localDateEncoder: Encoder[org.joda.time.LocalDate] = Encoder.encodeString.contramap(_.toString("yyyy-MM-dd"))
}
