package com.quadstingray.mongo.camp.model
import org.mongodb.scala.bson.{ Document, ObjectId }

import java.util.Date

case class DBFileInformation(_id: ObjectId, filename: String, length: Long, chunkSize: Long, uploadDate: Date, metadata: Option[Document])
