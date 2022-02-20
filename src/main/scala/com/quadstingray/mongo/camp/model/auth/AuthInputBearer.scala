package com.quadstingray.mongo.camp.model.auth

case class AuthInputBearer(bearerToken: Option[String]) extends AuthInput
