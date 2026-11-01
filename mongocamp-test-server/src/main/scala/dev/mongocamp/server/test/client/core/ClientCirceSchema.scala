package dev.mongocamp.server.test.client.core

import dev.mongocamp.server.converter.CirceSchema
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import java.util.Date
import org.bson.types.ObjectId
import org.joda.time.DateTime

// Instances of CirceSchema for the generated client api classes, WITHOUT Encoder[Any].
// sttp's BodySerializer is contravariant, so on Scala 3 an Encoder[Any] in lexical scope is chosen for every request body
// (None would be sent as {}); the Any instance therefore stays hidden inside ClientCirceSchema.instances.
trait ClientCirceSchema {
  implicit val DateFormat: Encoder[Date] & Decoder[Date]                                 = ClientCirceSchema.instances.DateFormat
  implicit val DateTimeFormat: Encoder[DateTime] & Decoder[DateTime]                     = ClientCirceSchema.instances.DateTimeFormat
  implicit val ObjectIdFormat: Encoder[ObjectId] & Decoder[ObjectId]                     = ClientCirceSchema.instances.ObjectIdFormat
  implicit val MapStringAnyFormat: Encoder[Map[String, Any]] & Decoder[Map[String, Any]] = ClientCirceSchema.instances.MapStringAnyFormat

  // decoding Any is needed for responses like Seq[Any]; a Decoder can not take part in the BodySerializer resolution
  implicit val AnyDecoder: Decoder[Any] = ClientCirceSchema.instances.AnyFormat

  def encodeAnyToJson(a: Any, deepth: Int = 0): Json = ClientCirceSchema.instances.encodeAnyToJson(a, deepth)
}

object ClientCirceSchema {
  private[core] object instances extends CirceSchema
}
