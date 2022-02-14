package com.quadstingray.mongo.rest.database
import com.quadstingray.mongo.rest.model.auth.UserRole
import com.sfxcode.nosql.mongo.MongoDAO

case class UserRolesDao() extends MongoDAO[UserRole](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameUserRoles)
