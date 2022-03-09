package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.client.api.AuthApi
import com.quadstingray.mongo.camp.client.model.{ Grant, Login, PasswordUpdateRequest }
import com.quadstingray.mongo.camp.database.MongoDatabase.tokenCacheDao
import com.quadstingray.mongo.camp.server.TestAdditions
import com.sfxcode.nosql.mongo._

class AuthSuite extends BaseSuite {

  val adminApi: AuthApi = AuthApi()

  test("login an logout user") {
    val login = executeRequestToResponse(adminApi.login(Login(TestAdditions.adminUser, TestAdditions.adminPassword)))
    assertEquals(login.expirationDate.isAfterNow, true)
    assertEquals(login.userProfile.user, "admin")
    assertEquals(
      login.userProfile.grants,
      Some(
        List(
          Grant("*", read = true, write = true, administrate = true, com.quadstingray.mongo.camp.model.auth.Grant.grantTypeBucket),
          Grant("*", read = true, write = true, administrate = true, com.quadstingray.mongo.camp.model.auth.Grant.grantTypeCollection)
        )
      )
    )
    val logout = executeRequestToResponse(adminApi.logout("", login.authToken)())
    assertEquals(logout.value, true)
  }

  test("login an logout user by delete") {
    val login = executeRequestToResponse(adminApi.login(Login(TestAdditions.adminUser, TestAdditions.adminPassword)))
    assertEquals(login.expirationDate.isAfterNow, true)
    assertEquals(login.userProfile.user, "admin")
    assertEquals(
      login.userProfile.grants,
      Some(
        List(
          Grant("*", read = true, write = true, administrate = true, com.quadstingray.mongo.camp.model.auth.Grant.grantTypeBucket),
          Grant("*", read = true, write = true, administrate = true, com.quadstingray.mongo.camp.model.auth.Grant.grantTypeCollection)
        )
      )
    )
    val logout = executeRequestToResponse(adminApi.logoutByDelete("", login.authToken)())
    assertEquals(logout.value, true)
  }

  test("refresh token") {
    val cacheCountBefore = tokenCacheDao.count().result()
    val refresh          = executeRequestToResponse(adminApi.refreshToken("", adminBearerToken)())
    val cacheCountAfter  = tokenCacheDao.count().result()
    assertEquals(cacheCountBefore + 1, cacheCountAfter)
    assertEquals(refresh.expirationDate.isAfterNow, true)
    assertEquals(refresh.userProfile.user, "admin")
    assertEquals(
      refresh.userProfile.grants,
      Some(
        List(
          Grant("*", read = true, write = true, administrate = true, com.quadstingray.mongo.camp.model.auth.Grant.grantTypeBucket),
          Grant("*", read = true, write = true, administrate = true, com.quadstingray.mongo.camp.model.auth.Grant.grantTypeCollection)
        )
      )
    )
  }

  test("update api key") {
    val updateResponse = executeRequestToResponse(adminApi.updateApiKey("", adminBearerToken)())
    assertEquals(updateResponse.value.isBlank, false)
    val userProfile = executeRequestToResponse(adminApi.userProfile("", adminBearerToken)())
    assertEquals(updateResponse.value, userProfile.apiKey.getOrElse("not_set"))
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
