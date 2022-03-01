package com.quadstingray.mongo.camp.database

import com.quadstingray.mongo.camp.model.auth.TokenCacheElement
import com.sfxcode.nosql.mongo.MongoDAO

case class TokenCacheDao() extends MongoDAO[TokenCacheElement](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameTokenCache)
