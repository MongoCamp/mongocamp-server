package com.quadstingray.mongo.rest.model

case class ReplaceResponse(wasAcknowledged: Boolean, upsertedIds: List[String], modifiedCount: Long, matchedCount: Long)
