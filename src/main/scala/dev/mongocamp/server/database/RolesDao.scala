package dev.mongocamp.server.database

import com.sfxcode.nosql.mongo.MongoDAO
import dev.mongocamp.server.model.auth.Role

case class RolesDao() extends MongoDAO[Role](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameRoles)
