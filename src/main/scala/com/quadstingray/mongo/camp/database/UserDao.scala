package com.quadstingray.mongo.camp.database
import com.quadstingray.mongo.camp.model.auth.UserInformation
import com.sfxcode.nosql.mongo.MongoDAO

case class UserDao() extends MongoDAO[UserInformation](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameUsers)
