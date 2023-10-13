package dev.mongocamp.server.model.auth

import sttp.tapir.model.UsernamePassword

case class AuthInputBasicWithApiKey(basic: Option[UsernamePassword], apiKey: Option[String]) extends AuthInput
