package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.model.auth.TokenCacheElement

case class TokenCacheDao() extends MongoDAO[TokenCacheElement](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameTokenCache)
