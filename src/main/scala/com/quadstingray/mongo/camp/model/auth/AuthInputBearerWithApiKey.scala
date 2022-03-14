package com.quadstingray.mongo.camp.model.auth

case class AuthInputBearerWithApiKey(bearerToken: Option[String], apiKey: Option[String]) extends AuthInput
