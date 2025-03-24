package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.model.auth.TokenCacheElement
import io.circe.generic.auto._

case class TokenCacheDao() extends MongoDAO[TokenCacheElement](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameTokenCache)
