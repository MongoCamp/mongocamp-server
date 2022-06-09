package dev.mongocamp.server.client.model

case class Metric(
    name: String,
    metricsType: String,
    description: String,
    baseUnit: String,
    measurements: Option[Seq[Measurement]] = None
)
