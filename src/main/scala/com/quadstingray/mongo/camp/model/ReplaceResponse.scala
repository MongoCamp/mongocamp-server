package com.quadstingray.mongo.camp.model

case class ReplaceResponse(wasAcknowledged: Boolean, upsertedIds: List[String], modifiedCount: Long, matchedCount: Long)
