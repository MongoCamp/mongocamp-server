package dev.mongocamp.server.model.auth

import java.util.Date

case class TokenCacheElement(token: String, userId: String, validTo: Date)
