package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.interceptor.RequestLogging

case class RequestLoggingDao() extends MongoDAO[RequestLogging](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameRequestLog)
