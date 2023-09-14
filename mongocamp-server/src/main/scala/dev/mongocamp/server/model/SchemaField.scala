package dev.mongocamp.server.model

case class SchemaField(
    name: String,
    fullName: String,
    fieldType: String,
    subFields: List[SchemaField]
)
