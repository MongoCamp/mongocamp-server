package dev.mongocamp.server.tests
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.database.MongoDaoHolder.tokenCacheDao
import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.model.ModelConstants
import dev.mongocamp.server.service.ConfigurationService
import dev.mongocamp.server.test.client.api.AuthApi
import dev.mongocamp.server.test.client.model
import dev.mongocamp.server.test.client.model.Grant
import dev.mongocamp.server.test.client.model.Login
import dev.mongocamp.server.test.client.model.PasswordUpdateRequest
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import scala.concurrent.duration.DurationInt

class AuthSuite extends MongoCampBaseServerSuite {

  lazy val adminApi: AuthApi = AuthApi()

  test("login an logout user") {
    val login = executeRequestToResponse(adminApi.login(Login(TestAdditions.adminUser, TestAdditions.adminPassword)))
    assertEquals(login.expirationDate.isAfterNow, true)
    assertEquals(login.userProfile.user, "admin")
    assertEquals(
      login.userProfile.grants,
      List(
        Grant("*", read = true, write = true, administrate = true, ModelConstants.grantTypeBucket),
        Grant("*", read = true, write = true, administrate = true, ModelConstants.grantTypeCollection)
      )
    )
    val logout = executeRequestToResponse(adminApi.logout("", "", login.authToken, "")())
    assertEquals(logout.value, true)
  }

  test("check admin is authenticated") {
    val authenticatedResponse = executeRequestToResponse(adminApi.isAuthenticated("", "", adminBearerToken, "")())
    assertEquals(authenticatedResponse.value, true)
  }

  test("login an logout user by delete") {
    val login = executeRequestToResponse(adminApi.login(Login(TestAdditions.adminUser, TestAdditions.adminPassword)))
    assertEquals(login.expirationDate.isAfterNow, true)
    assertEquals(login.userProfile.user, "admin")
    assertEquals(
      login.userProfile.grants,
      List(
        model.Grant("*", read = true, write = true, administrate = true, ModelConstants.grantTypeBucket),
        model.Grant("*", read = true, write = true, administrate = true, ModelConstants.grantTypeCollection)
      )
    )
    val logout = executeRequestToResponse(adminApi.logoutByDelete("", "", login.authToken, "")())
    assertEquals(logout.value, true)
  }

  test("refresh token") {
    clearAdminToken
    Thread.sleep(1.seconds.toMillis)
    val cacheCountBefore = tokenCacheDao.count().result()
    val refresh          = executeRequestToResponse(adminApi.refreshToken("", "", adminBearerToken, "")())
    Thread.sleep(1.seconds.toMillis)
    val cacheCountAfter = tokenCacheDao.count().result()
    assertEquals(cacheCountBefore < cacheCountAfter, true, "cacheCountBefore is not lower cacheCountAfter")
    assertEquals(refresh.expirationDate.isAfterNow, true)
    assertEquals(refresh.userProfile.user, "admin")
    assertEquals(
      refresh.userProfile.grants,
      List(
        model.Grant("*", read = true, write = true, administrate = true, ModelConstants.grantTypeBucket),
        model.Grant("*", read = true, write = true, administrate = true, ModelConstants.grantTypeCollection)
      )
    )
  }

  // #65: the token that was used for the refresh stops working after the grace period
  test("a refreshed token is invalid after the grace period") {
    val graceKey = DefaultConfigurations.ConfigKeyAuthRefreshGraceDuration
    assertEquals(ConfigurationService.updateConfig(graceKey, "1s"), true)
    try {
      val login = executeRequestToResponse(adminApi.login(Login(TestAdditions.adminUser, TestAdditions.adminPassword)))
      val first = executeRequestToResponse(adminApi.refreshToken("", "", login.authToken, "")())
      // a second refresh with the same old token inside the grace period works, concurrent clients and requests on the way
      val second = executeRequestToResponse(adminApi.refreshToken("", "", login.authToken, "")())
      assertEquals(first.authToken == second.authToken, false)
      assertEquals(executeRequest(adminApi.isAuthenticated("", "", login.authToken, "")()).code.code, 200, "old token inside the grace period")
      Thread.sleep(1500)
      assertEquals(executeRequest(adminApi.isAuthenticated("", "", login.authToken, "")()).code.code, 401, "old token after the grace period")
      assertEquals(executeRequest(adminApi.isAuthenticated("", "", first.authToken, "")()).code.code, 200, "new token")
      assertEquals(executeRequest(adminApi.isAuthenticated("", "", second.authToken, "")()).code.code, 200, "second new token")
    }
    finally {
      ConfigurationService.updateConfig(graceKey, "60s")
    }
  }

  test("a refresh with an api key does not revoke a token") {
    val login  = executeRequestToResponse(adminApi.login(Login(TestAdditions.adminUser, TestAdditions.adminPassword)))
    val apiKey = executeRequestToResponse(adminApi.generateNewApiKey("", "", login.authToken, "")()).value
    // the generated client always sends an Authorization header, an api key call has none
    val request = adminApi.refreshToken("", "", "", apiKey)()
    val refresh = executeRequestToResponse(request.copy(headers = request.headers.filterNot(_.is("Authorization"))))
    assertEquals(refresh.userProfile.user, "admin")
    assertEquals(executeRequest(adminApi.isAuthenticated("", "", login.authToken, "")()).code.code, 200)
  }

  test("update api key") {
    clearAdminToken
    val updateResponse = executeRequestToResponse(adminApi.generateNewApiKey("", "", adminBearerToken, "")())
    assertEquals(updateResponse.value.isBlank, false)
    val userProfile = executeRequestToResponse(adminApi.userProfile("", "", adminBearerToken, "")())
    assertEquals(updateResponse.value, userProfile.apiKey.getOrElse("not_set"))
  }

  test("token claims do not contain the api key") {
    clearAdminToken
    val apiKey  = executeRequestToResponse(adminApi.generateNewApiKey("", "", adminBearerToken, "")()).value
    val login   = executeRequestToResponse(adminApi.login(Login(TestAdditions.adminUser, TestAdditions.adminPassword)))
    val refresh = executeRequestToResponse(adminApi.refreshToken("", "", login.authToken, "")())
    // the owner of the token still gets the api key in the response, only the token itself must not carry it
    assertEquals(login.userProfile.apiKey, Some(apiKey))
    List(login.authToken, refresh.authToken).foreach(
      token => {
        val payload = new String(java.util.Base64.getUrlDecoder.decode(token.split('.')(1)), "UTF-8")
        assertEquals(payload.contains("apiKey"), false, s"token claims contain the apiKey field: $payload")
        assertEquals(payload.contains(apiKey), false, s"token claims contain the api key value: $payload")
        assertEquals(payload.contains("\"user\":\"admin\""), true, s"token claims lost the user: $payload")
      }
    )
  }

  test("check user is authenticated") {
    val authenticatedResponse = executeRequestToResponse(adminApi.isAuthenticated("", "", testUserBearerToken, "")())
    assertEquals(authenticatedResponse.value, true)
  }

  test("update password") {
    val newPassword    = "test1234"
    val updateResponse = executeRequestToResponse(adminApi.updatePassword("", "", testUserBearerToken, "")(PasswordUpdateRequest(newPassword)))
    assertEquals(updateResponse.value, true)
    val userProfile = executeRequestToResponse(adminApi.login(Login(TestAdditions.testUser, newPassword)))
    assertEquals(userProfile.userProfile.user, TestAdditions.testUser)
    executeRequestToResponse(adminApi.updatePassword("", "", testUserBearerToken, "")(PasswordUpdateRequest(TestAdditions.testPassword)))
  }

}
