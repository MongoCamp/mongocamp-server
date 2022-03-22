package dev.mongocamp.server.database

import com.sfxcode.nosql.mongo.MongoDAO
import dev.mongocamp.server.interceptor.RequestLogging

case class RequestLoggingDao() extends MongoDAO[RequestLogging](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameRequestLog)
