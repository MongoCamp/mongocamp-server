package com.quadstingray.mongo.camp.model.auth

case class UpdateUserRoleRequest(isAdmin: Boolean, collectionGrants: List[CollectionGrant]) {}
