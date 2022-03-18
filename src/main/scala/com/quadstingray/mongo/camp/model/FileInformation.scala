package com.quadstingray.mongo.camp.model
import com.quadstingray.mongo.camp.converter.MongoCampBsonConverter
import org.mongodb.scala.bson.ObjectId

import java.util.Date

case class FileInformation(_id: ObjectId, filename: String, length: Long, chunkSize: Long, uploadDate: Date, metadata: Map[String, Any] = Map())

object FileInformation {
  def apply(dbFileInformation: DBFileInformation): FileInformation = FileInformation(
    dbFileInformation._id,
    dbFileInformation.filename,
    dbFileInformation.length,
    dbFileInformation.chunkSize,
    dbFileInformation.uploadDate,
    dbFileInformation.metadata.map(MongoCampBsonConverter.documentToMap).getOrElse(Map())
  )
}
