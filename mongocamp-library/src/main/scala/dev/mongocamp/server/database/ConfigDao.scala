package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.driver.mongodb.json._
import org.mongodb.scala.bson.Document

case class ConfigDao() extends MongoDAO[Document](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameConfiguration)
