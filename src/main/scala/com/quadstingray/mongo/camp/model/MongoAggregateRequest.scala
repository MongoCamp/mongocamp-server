package com.quadstingray.mongo.camp.model

case class MongoAggregateRequest(pipeline: List[PipelineStage], allowDiskUse: Boolean = false)
