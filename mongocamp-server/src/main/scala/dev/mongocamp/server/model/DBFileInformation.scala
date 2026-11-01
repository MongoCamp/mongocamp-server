package dev.mongocamp.server.model

import java.util.Date
import org.mongodb.scala.bson.ObjectId

case class DBFileInformation(_id: ObjectId, filename: String, length: Long, chunkSize: Long, uploadDate: Date, metadata: Option[Map[String, Any]])
