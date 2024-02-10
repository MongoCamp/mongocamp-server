package dev.mongocamp.server.plugin.requestlogging.database

import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.plugin.requestlogging.listener.DatabaseRequestLoggingElement
import dev.mongocamp.server.database.MongoDatabase

case class RequestLoggingDao() extends MongoDAO[DatabaseRequestLoggingElement](MongoDatabase.databaseProvider, RequestLoggingDatabase.CollectionNameRequestLog)
