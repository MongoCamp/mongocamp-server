package com.quadstingray.mongo.rest.model.auth
import sttp.tapir.model.UsernamePassword

case class AuthInputAllMethods(bearerToken: Option[String], basic: Option[UsernamePassword], apiKey: Option[String]) extends AuthInput
