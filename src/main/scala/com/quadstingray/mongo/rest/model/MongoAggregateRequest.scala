package com.quadstingray.mongo.rest.model

case class MongoAggregateRequest(pipeline: List[PipelineStage], allowDiskUse: Boolean = false)
