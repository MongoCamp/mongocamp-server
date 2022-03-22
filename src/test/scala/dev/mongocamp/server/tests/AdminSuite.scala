package dev.mongocamp.server.tests

import dev.mongocamp.server.client.api.{ AdminApi, AuthApi }
import dev.mongocamp.server.client.model._

class AdminSuite extends BaseSuite {

  val adminApi: AdminApi = AdminApi()

  test("list users as admin") {
    val users = executeRequestToResponse(adminApi.listUsers("", adminBearerToken)())
    assertEquals(users.size, 2)
    assertEquals(users.exists(_.user == "admin"), true)
  }

  test("add user as admin") {
    val userInformation = UserInformation("myTestUser", "password", roles = Seq("test"))
    val user            = executeRequestToResponse(adminApi.addUser("", adminBearerToken)(userInformation))
    assertEquals(user.user, "myTestUser")
    assertEquals(user.roles, Seq("test"))
    val login = executeRequestToResponse(AuthApi().login(Login("myTestUser", "password")))
    assertEquals(login.userProfile.user, "myTestUser")
  }

  test("update apikey for user as admin") {
    val apiKey = executeRequestToResponse(adminApi.gnerateNewApiKeyForUser("", adminBearerToken)("myTestUser"))
    assertEquals(apiKey.value.isBlank, false)

    val user = executeRequestToResponse(adminApi.getUser("", adminBearerToken)("myTestUser"))
    assertEquals(user.apiKey, Some(apiKey.value))
  }

  test("update password for user as admin") {
    val apiKey = executeRequestToResponse(adminApi.updatePasswordForUser("", adminBearerToken)("myTestUser", PasswordUpdateRequest("new-password")))
    assertEquals(apiKey.value, true)

    val login = executeRequestToResponse(AuthApi().login(Login("myTestUser", "new-password")))
    assertEquals(login.expirationDate.isAfterNow, true)
  }

  test("update roles for user as admin") {
    val user = executeRequestToResponse(adminApi.updateRolesForUser("", adminBearerToken)("myTestUser", List("adminRole")))
    assertEquals(user.roles, List("adminRole"))
    assertEquals(
      user.grants,
      Seq(
        Grant("*", read = true, write = true, administrate = true, dev.mongocamp.server.model.auth.Grant.grantTypeBucket),
        Grant("*", read = true, write = true, administrate = true, dev.mongocamp.server.model.auth.Grant.grantTypeCollection)
      )
    )
  }

  test("delete user as admin") {
    val user = executeRequestToResponse(adminApi.deleteUser("", adminBearerToken)("myTestUser"))
    assertEquals(user.value, true)
    var loginWorked = true
    try {
      val login = executeRequestToResponse(AuthApi().login(Login("myTestUser", "new-password")))
      assertEquals(login.userProfile.user, "myTestUser")
    }
    catch {
      case _: Exception => loginWorked = false
    }
    assertEquals(loginWorked, false)
  }

  test("list roles as admin") {
    val roles = executeRequestToResponse(adminApi.listRoles("", adminBearerToken)())
    assertEquals(roles.size, 2)
    assertEquals(roles.exists(_.name == "adminRole"), true)
  }

  test("add role as admin") {
    val role = executeRequestToResponse(adminApi.addRole("", adminBearerToken)(Role("unitTestRole", isAdmin = false, List())))
    assertEquals(role.name, "unitTestRole")
    assertEquals(role.collectionGrants, List())
  }

  test("get role as admin") {
    val role = executeRequestToResponse(adminApi.getRole("", adminBearerToken)("unitTestRole"))
    assertEquals(role.name, "unitTestRole")
    assertEquals(role.isAdmin, false)
    assertEquals(role.collectionGrants, List())
  }

  test("update role as admin") {
    val role = executeRequestToResponse(
      adminApi.updateRole("", adminBearerToken)(
        "unitTestRole",
        UpdateRoleRequest(
          isAdmin = true,
          List(Grant("random", read = true, write = true, administrate = true, dev.mongocamp.server.model.auth.Grant.grantTypeCollection))
        )
      )
    )
    assertEquals(role.name, "unitTestRole")
    assertEquals(role.isAdmin, true)
    assertEquals(
      role.collectionGrants,
      List(Grant("random", read = true, write = true, administrate = true, dev.mongocamp.server.model.auth.Grant.grantTypeCollection))
    )
  }

  test("delete role as admin") {
    val role = executeRequestToResponse(adminApi.deleteRole("", adminBearerToken)("unitTestRole"))
    assertEquals(role.value, true)

    val roles = executeRequestToResponse(adminApi.listRoles("", adminBearerToken)())
    assertEquals(roles.size, 2)
    assertEquals(roles.exists(_.name == "unitTestRole"), false)
  }

  test("list users as user") {
    val response = executeRequest(adminApi.listUsers("", testUserBearerToken)())
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

  test("add user as user") {
    val userInformation = UserInformation("myTestUser", "password", roles = Seq("test"))
    val response        = executeRequest(adminApi.addUser("", testUserBearerToken)(userInformation))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

  test("update apikey for user as user") {
    val response = executeRequest(adminApi.gnerateNewApiKeyForUser("", testUserBearerToken)("myTestUser"))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")

  }

  test("update password for user as user") {
    val response = executeRequest(adminApi.updatePasswordForUser("", testUserBearerToken)("myTestUser", PasswordUpdateRequest("new-password")))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

  test("update roles for user as user") {
    val response = executeRequest(adminApi.updateRolesForUser("", testUserBearerToken)("myTestUser", List("adminRole")))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

  test("delete user as user") {
    val response = executeRequest(adminApi.deleteUser("", testUserBearerToken)("myTestUser"))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

  test("list roles as user") {
    val response = executeRequest(adminApi.listRoles("", testUserBearerToken)())
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

  test("add role as user") {
    val response = executeRequest(adminApi.addRole("", testUserBearerToken)(Role("unitTestRole", isAdmin = false, List())))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

  test("get role as user") {
    val response = executeRequest(adminApi.getRole("", testUserBearerToken)("unitTestRole"))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

  test("update role as user") {
    val response = executeRequest(
      adminApi.updateRole("", testUserBearerToken)(
        "unitTestRole",
        UpdateRoleRequest(
          isAdmin = true,
          List(Grant("random", read = true, write = true, administrate = true, dev.mongocamp.server.model.auth.Grant.grantTypeCollection))
        )
      )
    )
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

  test("delete role as user") {
    val response = executeRequest(adminApi.deleteRole("", testUserBearerToken)("unitTestRole"))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for request")
  }

}
