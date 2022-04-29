package dev.mongocamp.server.model

case class JsonSchemaDefinition(
    `type`: String,
    title: String,
    additionalProperties: Boolean,
    required: List[String],
    properties: Map[String, Map[String, Any]]
)
