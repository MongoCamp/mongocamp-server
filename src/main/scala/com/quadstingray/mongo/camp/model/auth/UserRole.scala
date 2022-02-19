package com.quadstingray.mongo.camp.model.auth

case class UserRole(name: String, isAdmin: Boolean, collectionGrants: List[CollectionGrant]) {}
