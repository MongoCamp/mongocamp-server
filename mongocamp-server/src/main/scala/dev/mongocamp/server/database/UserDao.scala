package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.model.auth.UserInformation
import io.circe.generic.auto._

case class UserDao() extends MongoDAO[UserInformation](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameUsers)
