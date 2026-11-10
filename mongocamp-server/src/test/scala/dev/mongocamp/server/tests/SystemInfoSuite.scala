package dev.mongocamp.server.tests

import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.test.client.api.SystemInfoApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import dev.mongocamp.server.test.TestServer
import sttp.client3.basicRequest
import sttp.client3.ignore
import sttp.client3.UriContext

class SystemInfoSuite extends MongoCampBaseServerSuite {

  lazy val systemInfoApi: SystemInfoApi = SystemInfoApi()

  test("system info as admin has the version of the database") {
    val info = executeRequestToResponse(systemInfoApi.systemInfo("", "", adminBearerToken, "")())
    assert(info.mongoDbVersionArray.size >= 3, s"the version numbers are missing: $info")
    assert(info.mongoDbVersionArray.head >= 6, s"MongoDB 6.0 and later is supported: $info")
    assert(info.mongoDbVersion.startsWith(info.mongoDbVersionArray.take(3).mkString(".")), s"version and numbers differ: $info")
  }

  test("system info as user is not authorized") {
    val response = executeRequest(systemInfoApi.systemInfo("", "", testUserBearerToken, "")())
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message"), Some("user not authorized for request"))
  }

  // the generated client always sends an Authorization header (an empty bearer is a 400), a request without any credentials is sent by hand
  test("system info without credentials is not authorized") {
    val response = TestAdditions.backend.send(basicRequest.get(uri"${TestServer.serverBaseUrl}/system/info").response(ignore))
    assertEquals(response.code.code, 401)
  }
}
