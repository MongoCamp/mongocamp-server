package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.event.listener.DatabaseRequestLoggingElement

case class RequestLoggingDao() extends MongoDAO[DatabaseRequestLoggingElement](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameRequestLog)
