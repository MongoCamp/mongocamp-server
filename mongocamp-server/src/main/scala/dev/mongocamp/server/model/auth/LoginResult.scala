package dev.mongocamp.server.model.auth

case class LoginResult(authToken: String, userProfile: UserProfile, expirationDate: java.util.Date)
