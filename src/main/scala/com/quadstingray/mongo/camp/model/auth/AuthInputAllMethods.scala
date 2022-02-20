package com.quadstingray.mongo.camp.model.auth
import sttp.tapir.model.UsernamePassword

case class AuthInputAllMethods(bearerToken: Option[String], basic: Option[UsernamePassword], apiKey: Option[String]) extends AuthInput
