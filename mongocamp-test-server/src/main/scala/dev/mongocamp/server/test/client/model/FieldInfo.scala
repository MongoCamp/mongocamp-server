package dev.mongocamp.server.test.client.model

case class FieldTypeCount(`type`: String, count: Long)

case class FieldInfo(path: String, types: Seq[FieldTypeCount])
