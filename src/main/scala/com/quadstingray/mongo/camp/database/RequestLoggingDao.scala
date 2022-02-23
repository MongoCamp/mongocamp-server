package com.quadstingray.mongo.camp.database

import com.quadstingray.mongo.camp.interceptor.RequestLogging
import com.sfxcode.nosql.mongo.MongoDAO

case class RequestLoggingDao() extends MongoDAO[RequestLogging](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameRequestLog)
