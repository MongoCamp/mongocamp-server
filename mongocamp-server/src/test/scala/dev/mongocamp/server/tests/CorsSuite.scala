package dev.mongocamp.server.tests

import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.DocumentSearchApi
import dev.mongocamp.server.test.client.model.MongoAggregateRequest
import dev.mongocamp.server.test.client.model.PipelineStage
import dev.mongocamp.server.test.client.model.SearchRequest
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import dev.mongocamp.server.test.TestServer
import sttp.client3._

class CorsSuite extends MongoCampBaseServerSuite {

  private val origin          = "http://localhost:3000"
  private val allowOriginKey  = "Access-Control-Allow-Origin"
  private val allowHeadersKey = "Access-Control-Allow-Headers"
  // must be a def: the base url must not be read before the test server was started by the base suite
  private def baseUrl: String = TestServer.serverBaseUrl

  private def assertCorsHeaders(response: Response[_], description: String): Unit = {
    assertEquals(response.header(allowOriginKey).exists(_.nonEmpty), true, s"$description (${response.code.code}) has no $allowOriginKey header")
    assertEquals(response.header(allowHeadersKey).exists(_.nonEmpty), true, s"$description (${response.code.code}) has no $allowHeadersKey header")
  }

  test("successful responses have cors headers") {
    val response = TestAdditions.backend.send(basicRequest.get(uri"$baseUrl/version").header("Origin", origin))
    assertEquals(response.code.code, 200)
    assertCorsHeaders(response, "version")
  }

  test("401 without credentials has cors headers") {
    val response = TestAdditions.backend.send(basicRequest.get(uri"$baseUrl/mongodb/collections").header("Origin", origin))
    assertEquals(response.code.code, 401)
    assertCorsHeaders(response, "request without token")
  }

  test("401 with an invalid token has cors headers") {
    val response = TestAdditions.backend.send(basicRequest.get(uri"$baseUrl/mongodb/collections").header("Origin", origin).auth.bearer("not.a.token"))
    assertEquals(response.code.code, 401)
    assertCorsHeaders(response, "request with an invalid token")
  }

  test("401 on login with a wrong password has cors headers") {
    val response = TestAdditions.backend.send(
      basicRequest.post(uri"$baseUrl/auth/login").header("Origin", origin).contentType("application/json").body("""{"userId":"admin","password":"wrong"}""")
    )
    assertEquals(response.code.code, 401)
    assertCorsHeaders(response, "login with a wrong password")
  }

  // a request that the database refuses because of its content is a 400, a real internal error cannot be provoked from outside
  test("a failed database request has cors headers") {
    val invalidLimit = MongoAggregateRequest(List(PipelineStage("limit", "abc")), allowDiskUse = false)
    val request      = CollectionApi().aggregate("", "", adminBearerToken, "")("geodata:locations", invalidLimit).header("Origin", origin)
    val response     = executeRequest(request)
    assertEquals(response.code.code, 400)
    assertCorsHeaders(response, "failed request")
  }

  test("the search route has cors headers, also for an invalid filter") {
    val ok = executeRequest(DocumentSearchApi().searchDocuments("", "", adminBearerToken, "")("accounts", SearchRequest(None)).header("Origin", origin))
    assertEquals(ok.code.code, 200)
    assertCorsHeaders(ok, "search")
    val invalid =
      executeRequest(DocumentSearchApi().searchDocuments("", "", adminBearerToken, "")("accounts", SearchRequest(Some("name:("))).header("Origin", origin))
    assertEquals(invalid.code.code, 400)
    assertCorsHeaders(invalid, "search with an invalid filter")
  }
}
