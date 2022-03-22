package dev.mongocamp.server.model

case class MongoAggregateRequest(pipeline: List[PipelineStage], allowDiskUse: Boolean = false)
