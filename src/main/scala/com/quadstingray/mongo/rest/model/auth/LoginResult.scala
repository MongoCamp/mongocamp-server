package com.quadstingray.mongo.rest.model.auth

case class LoginResult(authToken: String, userProfile: UserProfile, expirationDate: java.util.Date)

case class CollectionGrant(collection: String, read: Boolean, write: Boolean, administrate: Boolean)

case class UserProfile(user: String, isAdmin: Boolean, userRoles: List[String], collectionGrant: List[CollectionGrant])
