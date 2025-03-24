package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb.json._
import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.model.BucketInformation.BucketCollectionSuffix
import dev.mongocamp.server.model.DBFileInformation
import io.circe.generic.auto._

case class FileInformationDao(bucketName: String) extends MongoDAO[DBFileInformation](MongoDatabase.databaseProvider, s"$bucketName$BucketCollectionSuffix")
