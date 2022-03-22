package dev.mongocamp.server.model

case class UpdateResponse(wasAcknowledged: Boolean, upsertedIds: List[String], modifiedCount: Long, matchedCount: Long)
