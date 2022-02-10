package com.quadstingray.mongo.rest.model.auth

case class AuthInputBearer(bearerToken: Option[String]) extends AuthInput
