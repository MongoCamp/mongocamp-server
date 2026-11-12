package dev.mongocamp.server.model

/** The limits and settings of the server that change what a client can do. `grantDeniedStatus` is the status of a request without the grant (401 or 403),
  * `importMaxBytes` the size of the body of an import.
  */
case class CapabilityLimits(importMaxBytes: Long, grantDeniedStatus: Int)

/** What this server can do, so that a client adapts to it. `features` are the names of the features of this server (an older server has no such route, a client
  * takes that as the feature set of the old versions), `mongoDbMajor` is the major version of MongoDB (not given when the database does not answer),
  * `aggregationStagesDenied` the operators that an aggregation refuses.
  */
case class Capabilities(
  serverVersion: String,
  mongoDbMajor: Option[Int],
  features: List[String],
  limits: CapabilityLimits,
  aggregationStagesDenied: List[String]
)
