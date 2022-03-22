package dev.mongocamp.server.database

import com.sfxcode.nosql.mongo.MongoDAO
import dev.mongocamp.server.model.BucketInformation.BucketCollectionSuffix
import dev.mongocamp.server.model.DBFileInformation

case class FileInformationDao(bucketName: String) extends MongoDAO[DBFileInformation](MongoDatabase.databaseProvider, s"$bucketName$BucketCollectionSuffix")
