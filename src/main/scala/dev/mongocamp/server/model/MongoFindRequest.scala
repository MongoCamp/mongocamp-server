package dev.mongocamp.server.model

case class MongoFindRequest(filter: Map[String, Any], sort: Map[String, Any], projection: Map[String, Any])
