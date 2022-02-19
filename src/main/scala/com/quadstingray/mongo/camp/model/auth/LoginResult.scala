package com.quadstingray.mongo.camp.model.auth

case class LoginResult(authToken: String, userProfile: UserProfile, expirationDate: java.util.Date)
