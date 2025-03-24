package dev.mongocamp.server.plugin.requestlogging.database

import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.plugin.requestlogging.listener.DatabaseRequestLoggingElement
import io.circe.generic.auto._

case class RequestLoggingDao() extends MongoDAO[DatabaseRequestLoggingElement](MongoDatabase.databaseProvider, RequestLoggingDatabase.CollectionNameRequestLog)
