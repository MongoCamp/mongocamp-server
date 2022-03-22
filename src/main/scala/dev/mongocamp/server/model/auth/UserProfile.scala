package dev.mongocamp.server.model.auth

case class UserProfile(user: String, isAdmin: Boolean, apiKey: Option[String], roles: List[String], grants: List[Grant])
