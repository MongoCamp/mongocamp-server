package dev.mongocamp.server.database

import com.sfxcode.nosql.mongo.MongoDAO
import dev.mongocamp.server.model.auth.TokenCacheElement

case class TokenCacheDao() extends MongoDAO[TokenCacheElement](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameTokenCache)
