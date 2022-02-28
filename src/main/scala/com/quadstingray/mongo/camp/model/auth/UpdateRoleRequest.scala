package com.quadstingray.mongo.camp.model.auth

case class UpdateRoleRequest(isAdmin: Boolean, collectionGrants: List[CollectionGrant]) {}
