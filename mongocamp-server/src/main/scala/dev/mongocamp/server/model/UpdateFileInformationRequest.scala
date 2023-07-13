package dev.mongocamp.server.model

case class UpdateFileInformationRequest(filename: Option[String], metadata: Option[Map[String, Any]])
