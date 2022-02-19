package com.quadstingray.mongo.camp.model.auth

case class AuthInputWithApiKey(bearerToken: Option[String], apiKey: Option[String]) extends AuthInput
