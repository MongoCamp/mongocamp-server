package dev.mongocamp.server.test.client.model

case class CapabilityLimits(importMaxBytes: Long, grantDeniedStatus: Int)

case class Capabilities(
  serverVersion: String,
  mongoDbMajor: Option[Int] = None,
  features: Seq[String],
  limits: CapabilityLimits,
  aggregationStagesDenied: Seq[String]
)
