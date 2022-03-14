package com.quadstingray.mongo.camp.model.auth
import sttp.tapir.model.UsernamePassword

case class AuthInputBearerWithBasic(bearerToken: Option[String], basic: Option[UsernamePassword]) extends AuthInput
