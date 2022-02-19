package com.quadstingray.mongo.camp.model.auth

case class UserProfile(user: String, isAdmin: Boolean, apiKey: Option[String], userRoles: List[String], collectionGrant: List[CollectionGrant])
