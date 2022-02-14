package com.quadstingray.mongo.rest.model.auth

case class AuthInputWithApiKey(bearerToken: Option[String], apiKey: Option[String]) extends AuthInput
