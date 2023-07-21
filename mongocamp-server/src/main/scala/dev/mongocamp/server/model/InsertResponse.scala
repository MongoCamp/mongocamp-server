package dev.mongocamp.server.model

case class InsertResponse(wasAcknowledged: Boolean, insertedIds: List[String])
