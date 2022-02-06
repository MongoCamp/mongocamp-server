package com.quadstingray.mongo.rest.model

case class UpdateResponse(wasAcknowledged: Boolean, upsertedIds: List[String], modifiedCount: Long, matchedCount: Long)
