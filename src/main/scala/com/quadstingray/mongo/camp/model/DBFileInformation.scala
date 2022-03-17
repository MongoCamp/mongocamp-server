package com.quadstingray.mongo.camp.model
import org.mongodb.scala.bson.Document

import java.util.Date

case class DBFileInformation(filename: String, length: Long, chunkSize: Long, uploadDate: Date, metadata: Option[Document])
