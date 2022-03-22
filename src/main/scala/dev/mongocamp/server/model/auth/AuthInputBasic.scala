package dev.mongocamp.server.model.auth

import sttp.tapir.model.UsernamePassword

case class AuthInputBasic(basic: Option[UsernamePassword]) extends AuthInput
