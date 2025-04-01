package dev.mongocamp.server.model.auth

import org.joda.time.DateTime

case class TokenCacheElement(token: String, userId: String, validTo: DateTime)
