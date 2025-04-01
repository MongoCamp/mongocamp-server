package dev.mongocamp.server.model

import org.mongodb.scala.bson.ObjectId

import java.util.Date

case class DBFileInformation(_id: ObjectId, filename: String, length: Long, chunkSize: Long, uploadDate: Date, metadata: Option[Map[String, Any]])
