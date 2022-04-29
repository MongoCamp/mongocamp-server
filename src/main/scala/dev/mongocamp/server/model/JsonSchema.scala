package dev.mongocamp.server.model

case class JsonSchema(`$schema`: String, `$ref`: String, definitions: Map[String, JsonSchemaDefinition])

object JsonSchema {
  def apply(objectName: String, definitions: Map[String, JsonSchemaDefinition]): JsonSchema = {
    JsonSchema("http://json-schema.org/draft-09/schema#", s"#/definitions/$objectName", definitions)
  }
}
