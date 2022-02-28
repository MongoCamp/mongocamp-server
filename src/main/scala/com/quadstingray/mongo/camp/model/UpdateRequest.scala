package com.quadstingray.mongo.camp.model

case class UpdateRequest(document: Map[String, Any], filter: Map[String, Any] = Map())
