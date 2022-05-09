package dev.mongocamp.server.file

import better.files.File
import dev.mongocamp.driver.mongodb.{ GenericObservable, _ }
import dev.mongocamp.server.converter.MongoCampBsonConverter.convertIdField
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.model.BucketInformation.GridFsBucketChunksSuffix
import org.mongodb.scala.bson.ObjectId

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
    val deleteResult = MongoDatabase.databaseProvider.dao(s"$bucket$GridFsBucketChunksSuffix").deleteMany(Map("files_id" -> new ObjectId(fileId))).result()
    deleteResult.wasAcknowledged() && deleteResult.getDeletedCount == 1
  }

}
