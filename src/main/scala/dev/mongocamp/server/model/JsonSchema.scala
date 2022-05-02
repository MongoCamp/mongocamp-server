package dev.mongocamp.server.model

case class JsonSchema(`$schema`: String, `$ref`: String, definitions: Map[String, JsonSchemaDefinition])

object JsonSchema {
  def apply(objectName: String, definitions: Map[String, JsonSchemaDefinition]): JsonSchema = {
    JsonSchema("https://json-schema.org/draft/2020-12/schema", s"#/definitions/$objectName", definitions)
  }
}
