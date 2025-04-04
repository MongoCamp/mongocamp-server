package dev.mongocamp.server.model
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
    dbFileInformation.metadata.getOrElse(Map())
  )
}
