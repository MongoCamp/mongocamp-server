package dev.mongocamp.server.model.auth

case class Role(name: String, isAdmin: Boolean, collectionGrants: List[Grant]) {}
