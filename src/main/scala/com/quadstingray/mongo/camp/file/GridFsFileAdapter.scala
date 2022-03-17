package com.quadstingray.mongo.camp.file
import better.files.File
import com.quadstingray.mongo.camp.converter.MongoCampBsonConverter.convertIdField
import com.quadstingray.mongo.camp.database.MongoDatabase
import com.quadstingray.mongo.camp.model.BucketInformation.GridFsBucketChunksSuffix
import com.sfxcode.nosql.mongo.{ GenericObservable, _ }

class GridFsFileAdapter extends FilePlugin {
  override val name: String = "gridfs"

  override def getFile(bucket: String, fileId: String): File = {
    object FilesDAO extends GridFSDAO(MongoDatabase.databaseProvider, bucket)
    val tmpFile = File.newTemporaryFile()
    FilesDAO.downloadFileResult(convertIdField(fileId), tmpFile)
    tmpFile
  }

  override def putFile(bucket: String, fileId: String, file: File): Boolean = ???

  override def size(bucket: String): Double = {
    MongoDatabase.databaseProvider.dao(s"$bucket$GridFsBucketChunksSuffix").collectionStatus.result().size
  }

  override def delete(bucket: String): Unit = {
    MongoDatabase.databaseProvider.dao(s"$bucket$GridFsBucketChunksSuffix").drop().result()
  }

  override def clear(bucket: String): Boolean = {
    MongoDatabase.databaseProvider.dao(s"$bucket$GridFsBucketChunksSuffix").deleteAll().result().wasAcknowledged()
  }

  override def deleteFile(bucket: String, fileId: String): Boolean = {
    val deleteResult = MongoDatabase.databaseProvider.dao(s"$bucket$GridFsBucketChunksSuffix").deleteMany(Map("files_id" -> convertIdField(fileId))).result()
    deleteResult.wasAcknowledged()
  }

}
