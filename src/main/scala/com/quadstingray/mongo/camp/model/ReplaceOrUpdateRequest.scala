package com.quadstingray.mongo.camp.model

case class ReplaceOrUpdateRequest(document: Map[String, Any], filter: Map[String, Any] = Map())
