package dev.mongocamp.server.tests
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.client.api.AuthApi
import dev.mongocamp.server.client.model
import dev.mongocamp.server.client.model.{Grant, Login, PasswordUpdateRequest}
import dev.mongocamp.server.database.MongoDatabase.tokenCacheDao
import dev.mongocamp.server.model.auth
import dev.mongocamp.server.server.TestAdditions

import scala.concurrent.duration.DurationInt

class AuthSuite extends BaseSuite {

  val adminApi: AuthApi = AuthApi()

  test("login an logout user") {
    val login = executeRequestToResponse(adminApi.login(Login(TestAdditions.adminUser, TestAdditions.adminPassword)))
    assertEquals(login.expirationDate.isAfterNow, true)
    assertEquals(login.userProfile.user, "admin")
    assertEquals(
      login.userProfile.grants,
      List(
        Grant("*", read = true, write = true, administrate = true, dev.mongocamp.server.model.auth.Grant.grantTypeBucket),
        Grant("*", read = true, write = true, administrate = true, dev.mongocamp.server.model.auth.Grant.grantTypeCollection)
      )
    )
    val logout = executeRequestToResponse(adminApi.logout("", login.authToken)())
    assertEquals(logout.value, true)
  }

  test("check admin is authenticated") {
    val authenticatedResponse = executeRequestToResponse(adminApi.isAuthenticated("", adminBearerToken)())
    assertEquals(authenticatedResponse.value, true)
  }

  test("login an logout user by delete") {
    val login = executeRequestToResponse(adminApi.login(Login(TestAdditions.adminUser, TestAdditions.adminPassword)))
    assertEquals(login.expirationDate.isAfterNow, true)
    assertEquals(login.userProfile.user, "admin")
    assertEquals(
      login.userProfile.grants,
      List(
        model.Grant("*", read = true, write = true, administrate = true, auth.Grant.grantTypeBucket),
        model.Grant("*", read = true, write = true, administrate = true, auth.Grant.grantTypeCollection)
      )
    )
    val logout = executeRequestToResponse(adminApi.logoutByDelete("", login.authToken)())
    assertEquals(logout.value, true)
  }

  test("refresh token") {
    clearAdminToken
    Thread.sleep(1.seconds.toMillis)
    val cacheCountBefore = tokenCacheDao.count().result()
    val refresh          = executeRequestToResponse(adminApi.refreshToken("", adminBearerToken)())
    Thread.sleep(1.seconds.toMillis)
    val cacheCountAfter  = tokenCacheDao.count().result()
    assertEquals(cacheCountBefore < cacheCountAfter, true, "cacheCountBefore is not lower cacheCountAfter")
    assertEquals(refresh.expirationDate.isAfterNow, true)
    assertEquals(refresh.userProfile.user, "admin")
    assertEquals(
      refresh.userProfile.grants,
      List(
        model.Grant("*", read = true, write = true, administrate = true, auth.Grant.grantTypeBucket),
        model.Grant("*", read = true, write = true, administrate = true, auth.Grant.grantTypeCollection)
      )
    )
  }

  test("update api key") {
    clearAdminToken
    val updateResponse = executeRequestToResponse(adminApi.generateNewApiKey("", adminBearerToken)())
    assertEquals(updateResponse.value.isBlank, false)
    val userProfile = executeRequestToResponse(adminApi.userProfile("", adminBearerToken)())
    assertEquals(updateResponse.value, userProfile.apiKey.getOrElse("not_set"))
  }

  test("check user is authenticated") {
    val authenticatedResponse = executeRequestToResponse(adminApi.isAuthenticated("", testUserBearerToken)())
    assertEquals(authenticatedResponse.value, true)
  }

  test("update password") {
    val newPassword    = "test1234"
    val updateResponse = executeRequestToResponse(adminApi.updatePassword("", testUserBearerToken)(PasswordUpdateRequest(newPassword)))
    assertEquals(updateResponse.value, true)
    val userProfile = executeRequestToResponse(adminApi.login(Login(TestAdditions.testUser, newPassword)))
    assertEquals(userProfile.userProfile.user, TestAdditions.testUser)
    executeRequestToResponse(adminApi.updatePassword("", testUserBearerToken)(PasswordUpdateRequest(TestAdditions.testPassword)))
  }

}
