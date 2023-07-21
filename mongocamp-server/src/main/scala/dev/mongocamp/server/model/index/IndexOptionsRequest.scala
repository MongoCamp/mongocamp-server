package dev.mongocamp.server.model.index

case class IndexOptionsRequest(
    name: Option[String],
    background: Option[Boolean],
    defaultLanguage: Option[String],
    textVersion: Option[Int],
    expireAfter: Option[String],
    unique: Option[Boolean],
    max: Option[Double],
    min: Option[Double]
)
