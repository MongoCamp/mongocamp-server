package dev.mongocamp.server.tests

import dev.mongocamp.server.test.client.api.AdminApi
import dev.mongocamp.server.test.client.api.AuthApi
import dev.mongocamp.server.test.client.model._
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import sttp.model.StatusCode

class TokenRevocationSuite extends MongoCampBaseServerSuite {

  lazy val adminApi: AdminApi = AdminApi()
  lazy val authApi: AuthApi   = AuthApi()

  private val userId = "tokenRevocationUser"

  private def withUser(password: String, roles: Seq[String] = Seq("test"))(body: => Unit): Unit = {
    executeRequestToResponse(adminApi.addUser("", "", adminBearerToken, "")(UserInformation(userId, password, roles = roles)))
    try body
    finally executeRequest(adminApi.deleteUser("", "", adminBearerToken, "")(userId))
  }

  private def login(password: String): String = executeRequestToResponse(authApi.login(Login(userId, password))).authToken

  private def authenticatedStatus(token: String): Int = executeRequest(authApi.isAuthenticated("", "", token, "")()).code.code

  test("changing the own password revokes the other tokens and keeps the token of the call") {
    withUser("password-1") {
      val current = login("password-1")
      val other   = login("password-1")
      assertEquals(authenticatedStatus(other), StatusCode.Ok.code)
      val updated = executeRequestToResponse(authApi.updatePassword("", "", current, "")(PasswordUpdateRequest("password-2")))
      assertEquals(updated.value, true)
      assertEquals(authenticatedStatus(current), StatusCode.Ok.code)
      assertEquals(authenticatedStatus(other), StatusCode.Unauthorized.code)
    }
  }

  test("an admin changing the password revokes all tokens of the user") {
    withUser("password-1") {
      val first  = login("password-1")
      val second = login("password-1")
      executeRequestToResponse(adminApi.updatePasswordForUser("", "", adminBearerToken, "")(userId, PasswordUpdateRequest("password-2")))
      assertEquals(authenticatedStatus(first), StatusCode.Unauthorized.code)
      assertEquals(authenticatedStatus(second), StatusCode.Unauthorized.code)
      assertEquals(authenticatedStatus(login("password-2")), StatusCode.Ok.code)
    }
  }

  test("deleting a user revokes the tokens of the user") {
    withUser("password-1") {
      val token = login("password-1")
      assertEquals(executeRequestToResponse(adminApi.deleteUser("", "", adminBearerToken, "")(userId)).value, true)
      assertEquals(authenticatedStatus(token), StatusCode.Unauthorized.code)
    }
  }

  test("changed roles apply to the existing tokens right away") {
    withUser("password-1") {
      val token = login("password-1")
      assertNotEquals(executeRequest(adminApi.listUsers("", "", token, "")()).code.code, StatusCode.Ok.code)
      executeRequestToResponse(adminApi.updateRolesForUser("", "", adminBearerToken, "")(userId, List("adminRole")))
      assertEquals(executeRequest(adminApi.listUsers("", "", token, "")()).code.code, StatusCode.Ok.code)
    }
  }
}
