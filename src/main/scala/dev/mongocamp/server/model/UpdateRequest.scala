package dev.mongocamp.server.model

case class UpdateRequest(document: Map[String, Any], filter: Map[String, Any] = Map())
