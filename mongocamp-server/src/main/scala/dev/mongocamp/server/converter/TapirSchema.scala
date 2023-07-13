package dev.mongocamp.server.converter

import org.bson.types.ObjectId
import sttp.tapir.{Schema, SchemaType}

trait TapirSchema extends CirceSchema {

  implicit val schemaForObjectId: Schema[ObjectId] = Schema.string

  implicit val schemaAny: Schema[Any]                          = Schema(SchemaType.SString())
  implicit val schemaForMapStringAny: Schema[Map[String, Any]] = Schema.schemaForMap[Any]

}
