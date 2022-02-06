package com.quadstingray.mongo.rest.model

case class ReplaceOrUpdateRequest(document: Map[String, Any], filter: Map[String, Any] = Map())
