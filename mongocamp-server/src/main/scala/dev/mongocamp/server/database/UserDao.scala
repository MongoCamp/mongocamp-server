package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.model.auth.UserInformation

case class UserDao() extends MongoDAO[UserInformation](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameUsers)
