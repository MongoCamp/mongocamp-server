package dev.mongocamp.server.model.auth

case class AuthInputToken(apiKey: Option[String]) extends AuthInput
