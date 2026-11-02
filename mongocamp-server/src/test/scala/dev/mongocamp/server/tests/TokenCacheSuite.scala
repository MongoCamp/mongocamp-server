package dev.mongocamp.server.tests

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.auth.TokenCache
import dev.mongocamp.server.database.MongoDaoHolder.tokenCacheDao
import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.test.client.api.AuthApi
import dev.mongocamp.server.test.client.model.Login
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import scala.concurrent.duration.DurationInt

class TokenCacheSuite extends MongoCampBaseServerSuite {

  private def newToken(): String = executeRequestToResponse(AuthApi().login(Login(TestAdditions.adminUser, TestAdditions.adminPassword))).authToken

  test("tokens of the same user in the same second are different") {
    val tokens = (1 to 5).map(
      _ => newToken()
    )
    assertEquals(tokens.distinct.size, tokens.size)
  }

  test("a superseded token is valid inside the grace period and invalid after it") {
    val token = newToken()
    assertEquals(TokenCache.validateToken(token).isDefined, true)
    TokenCache.supersede(token, 1.second)
    assertEquals(TokenCache.validateToken(token).isDefined, true, "invalid inside the grace period")
    Thread.sleep(1300)
    assertEquals(TokenCache.validateToken(token).isDefined, false, "valid after the grace period")
  }

  test("superseding one token does not touch the others") {
    val old   = newToken()
    val other = newToken()
    TokenCache.supersede(old, 0.seconds)
    assertEquals(TokenCache.validateToken(old).isDefined, false)
    assertEquals(TokenCache.validateToken(other).isDefined, true)
  }

  test("superseding shortens the entry in the database") {
    val token = newToken()
    Thread.sleep(300)
    TokenCache.supersede(token, 30.seconds)
    Thread.sleep(500)
    val entry = tokenCacheDao.find(Map(TokenCache.keyToken -> token)).resultOption()
    assertEquals(entry.isDefined, true)
    assertEquals(entry.get.validTo.isBefore(System.currentTimeMillis() + 31000), true)
  }

  test("a token that is superseded twice keeps the earlier end") {
    val token = newToken()
    TokenCache.supersede(token, 0.seconds)
    TokenCache.supersede(token, 1.hour)
    assertEquals(TokenCache.validateToken(token).isDefined, false)
  }
}
