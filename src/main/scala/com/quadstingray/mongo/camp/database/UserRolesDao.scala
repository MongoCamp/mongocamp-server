package com.quadstingray.mongo.camp.database
import com.quadstingray.mongo.camp.model.auth.UserRole
import com.sfxcode.nosql.mongo.MongoDAO

case class UserRolesDao() extends MongoDAO[UserRole](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameUserRoles)
