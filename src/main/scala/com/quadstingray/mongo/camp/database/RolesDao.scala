package com.quadstingray.mongo.camp.database
import com.quadstingray.mongo.camp.model.auth.Role
import com.sfxcode.nosql.mongo.MongoDAO

case class RolesDao() extends MongoDAO[Role](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameRoles)
