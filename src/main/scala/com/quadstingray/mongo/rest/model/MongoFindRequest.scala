package com.quadstingray.mongo.rest.model

case class MongoFindRequest(filter: Map[String, Any], sort: Map[String, Any], projection: Map[String, Any])
