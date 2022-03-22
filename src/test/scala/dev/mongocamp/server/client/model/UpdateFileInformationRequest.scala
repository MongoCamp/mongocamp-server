package dev.mongocamp.server.client.model

case class UpdateFileInformationRequest(
    filename: Option[String] = None,
    metadata: Option[Map[String, Any]] = None
)
