package dev.mongocamp.server.database

import com.sfxcode.nosql.mongo.MongoDAO
import dev.mongocamp.server.model.auth.UserInformation

case class UserDao() extends MongoDAO[UserInformation](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameUsers)
