package com.quadstingray.mongo.rest.model

case class InsertResponse(wasAcknowledged: Boolean, insertedIds: List[String])
