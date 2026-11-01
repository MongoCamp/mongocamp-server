package dev.mongocamp.server.converter

import org.bson.types.ObjectId
import sttp.tapir.Schema

trait TapirSchema extends CirceSchema {

  implicit val schemaForObjectId: Schema[ObjectId] = Schema.string

  // any JSON value (number, string, boolean, null, object, array): stage values, document and filter bodies and the response of distinct are not strings
  implicit val schemaAny: Schema[Any]                          = Schema.any[Any]
  implicit val schemaForMapStringAny: Schema[Map[String, Any]] = Schema.schemaForMap[Any]

}
