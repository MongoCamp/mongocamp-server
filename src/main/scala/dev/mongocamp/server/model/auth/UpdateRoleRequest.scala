package dev.mongocamp.server.model.auth

case class UpdateRoleRequest(isAdmin: Boolean, collectionGrants: List[Grant]) {}
