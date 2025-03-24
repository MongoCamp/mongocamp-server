package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.model.auth.Role
import io.circe.generic.auto._

case class RolesDao() extends MongoDAO[Role](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameRoles)
