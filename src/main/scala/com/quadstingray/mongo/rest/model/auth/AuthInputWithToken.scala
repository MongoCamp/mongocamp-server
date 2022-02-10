package com.quadstingray.mongo.rest.model.auth

case class AuthInputWithToken(bearerToken: Option[String], apiToken: Option[String]) extends AuthInput
