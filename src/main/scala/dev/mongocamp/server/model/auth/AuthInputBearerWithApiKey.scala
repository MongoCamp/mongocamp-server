package dev.mongocamp.server.model.auth

case class AuthInputBearerWithApiKey(bearerToken: Option[String], apiKey: Option[String]) extends AuthInput
