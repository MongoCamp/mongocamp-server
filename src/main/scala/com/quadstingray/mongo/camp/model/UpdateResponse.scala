package com.quadstingray.mongo.camp.model

case class UpdateResponse(wasAcknowledged: Boolean, upsertedIds: List[String], modifiedCount: Long, matchedCount: Long)
