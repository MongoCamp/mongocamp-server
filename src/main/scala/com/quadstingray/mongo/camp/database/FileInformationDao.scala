package com.quadstingray.mongo.camp.database

import com.quadstingray.mongo.camp.model.BucketInformation.BucketCollectionSuffix
import com.quadstingray.mongo.camp.model.DBFileInformation
import com.sfxcode.nosql.mongo.MongoDAO

case class FileInformationDao(bucketName: String) extends MongoDAO[DBFileInformation](MongoDatabase.databaseProvider, s"$bucketName$BucketCollectionSuffix")
