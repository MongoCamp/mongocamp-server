package com.quadstingray.mongo.rest.database

import com.quadstingray.mongo.rest.model.auth._
import com.sfxcode.nosql.mongo.MongoDAO

case class RoleGrantsDao() extends MongoDAO[UserRoleGrant](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameRoleGrants)
