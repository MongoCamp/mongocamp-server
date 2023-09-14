package dev.mongocamp.server.model.auth

case class AuthInputBearer(bearerToken: Option[String]) extends AuthInput
