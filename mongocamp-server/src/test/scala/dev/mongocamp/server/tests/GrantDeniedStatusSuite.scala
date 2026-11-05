package dev.mongocamp.server.tests

import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.service.ConfigurationService
import dev.mongocamp.server.test.client.api.AdminApi
import dev.mongocamp.server.test.client.api.BucketApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite

// a user without a grant is refused with the configured status (AUTH_GRANT_DENIED_STATUS), a missing or invalid token stays 401
class GrantDeniedStatusSuite extends MongoCampBaseServerSuite {

  private val key = DefaultConfigurations.ConfigKeyAuthGrantDeniedStatus

  lazy val documentsApi: DocumentApi = DocumentApi()
  lazy val bucketApi: BucketApi      = BucketApi()
  lazy val adminApi: AdminApi        = AdminApi()

  private def deniedResponses() = List(
    (
      "collection",
      "user not authorized for collection",
      executeRequest(documentsApi.listDocuments("", "", testUserBearerToken, "")("other", None, Seq(), Seq()))
    ),
    ("bucket", "user not authorized for bucket", executeRequest(bucketApi.getBucket("", "", testUserBearerToken, "")("sample-files"))),
    ("admin", "user not authorized for request", executeRequest(adminApi.listUsers("", "", testUserBearerToken, "")()))
  )

  private def assertDenied(status: Int): Unit =
    deniedResponses().foreach {
      case (name, message, response) =>
        assertEquals(response.code.code, status, s"$name route")
        assertEquals(response.header("x-error-message"), Some(message), s"$name route")
        assertEquals(response.header("x-error-code"), Some(ErrorCodes.unauthorizedUser.toString), s"$name route")
    }

  test("a missing grant answers 401 by default") {
    assertDenied(401)
  }

  test("a missing grant answers 403 when configured") {
    assertEquals(ConfigurationService.updateConfig(key, 403L), true)
    try {
      assertDenied(403)
      // the token is still fine: the session of the user was not touched
      assertEquals(executeRequest(documentsApi.listDocuments("", "", testUserBearerToken, "")("accounts", None, Seq(), Seq())).code.code, 200)
    }
    finally ConfigurationService.updateConfig(key, 401L)
  }

  test("a missing or invalid token is 401 with either setting") {
    assertEquals(ConfigurationService.updateConfig(key, 403L), true)
    try assertEquals(executeRequest(documentsApi.listDocuments("", "", "invalid", "")("accounts", None, Seq(), Seq())).code.code, 401)
    finally ConfigurationService.updateConfig(key, 401L)
  }

  test("any other value is treated as 401") {
    assertEquals(ConfigurationService.updateConfig(key, 500L), true)
    try assertDenied(401)
    finally ConfigurationService.updateConfig(key, 401L)
  }
}
