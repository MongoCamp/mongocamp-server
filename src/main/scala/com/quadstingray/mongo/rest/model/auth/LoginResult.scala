package com.quadstingray.mongo.rest.model.auth

case class LoginResult(authToken: String, userProfile: UserProfile, expirationDate: java.util.Date)
