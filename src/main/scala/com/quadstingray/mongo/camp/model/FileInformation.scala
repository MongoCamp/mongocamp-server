package com.quadstingray.mongo.camp.model
import com.quadstingray.mongo.camp.converter.MongoCampBsonConverter

import java.util.Date

case class FileInformation(filename: String, length: Long, chunkSize: Long, uploadDate: Date, metadata: Map[String, Any] = Map())

object FileInformation {
  def apply(dbFileInformation: DBFileInformation): FileInformation = FileInformation(
    dbFileInformation.filename,
    dbFileInformation.length,
    dbFileInformation.chunkSize,
    dbFileInformation.uploadDate,
    dbFileInformation.metadata.map(MongoCampBsonConverter.documentToMap).getOrElse(Map())
  )
}
