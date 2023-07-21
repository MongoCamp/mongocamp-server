package dev.mongocamp.server.model.auth

case class Grant(name: String, read: Boolean, write: Boolean, administrate: Boolean, grantType: String)

