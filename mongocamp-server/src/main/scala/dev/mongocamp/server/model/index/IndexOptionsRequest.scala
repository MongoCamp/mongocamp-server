package dev.mongocamp.server.model.index

case class IndexOptionsRequest(
  name: Option[String],
  background: Option[Boolean],
  defaultLanguage: Option[String],
  textVersion: Option[Int],
  expireAfter: Option[String],
  unique: Option[Boolean],
  max: Option[Double],
  min: Option[Double],
  sparse: Option[Boolean] = None,
  hidden: Option[Boolean] = None,
  partialFilterExpression: Option[Map[String, Any]] = None,
  weights: Option[Map[String, Int]] = None,
  collation: Option[Map[String, Any]] = None
)
