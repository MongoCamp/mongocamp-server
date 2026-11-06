package dev.mongocamp.server.test.client.model

case class IndexKey(field: String, direction: String)

case class IndexInfo(
  name: String,
  keys: Seq[IndexKey],
  unique: Boolean,
  sparse: Boolean,
  hidden: Boolean,
  partialFilterExpression: Option[Map[String, Any]],
  collation: Option[Map[String, Any]],
  weights: Option[Map[String, Any]],
  defaultLanguage: Option[String],
  expireAfterSeconds: Option[Long],
  version: Int
)
