package com.quadstingray.mongo.rest.database
import com.quadstingray.mongo.rest.model.auth.UserInformation
import com.sfxcode.nosql.mongo.MongoDAO

case class UserDao() extends MongoDAO[UserInformation](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameUsers)
