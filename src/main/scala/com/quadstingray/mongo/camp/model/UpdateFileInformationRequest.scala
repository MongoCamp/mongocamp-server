package com.quadstingray.mongo.camp.model

case class UpdateFileInformationRequest(filename: Option[String], metadata: Option[Map[String, Any]])
