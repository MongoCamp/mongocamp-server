package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb.MongoDAO
import org.mongodb.scala.bson.Document

case class ConfigDao() extends MongoDAO[Document](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameConfiguration)
