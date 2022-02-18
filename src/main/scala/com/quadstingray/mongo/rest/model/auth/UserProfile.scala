package com.quadstingray.mongo.rest.model.auth

case class UserProfile(user: String, isAdmin: Boolean, apiKey: Option[String], userRoles: List[String], collectionGrant: List[CollectionGrant])
