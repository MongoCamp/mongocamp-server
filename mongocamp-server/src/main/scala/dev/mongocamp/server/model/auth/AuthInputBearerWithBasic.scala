package dev.mongocamp.server.model.auth

import sttp.tapir.model.UsernamePassword

case class AuthInputBearerWithBasic(bearerToken: Option[String], basic: Option[UsernamePassword]) extends AuthInput
