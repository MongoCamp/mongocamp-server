package dev.mongocamp.server.tests

import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.test.client.api.SystemStatsApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import dev.mongocamp.server.test.TestServer
import sttp.client3.asString
import sttp.client3.basicRequest
import sttp.client3.ignore
import sttp.client3.UriContext

class SystemStatsSuite extends MongoCampBaseServerSuite {

  lazy val systemStatsApi: SystemStatsApi = SystemStatsApi()

  private def stats = executeRequestToResponse(systemStatsApi.systemStats("", "", adminBearerToken, "")())

  test("system stats as admin have uptime, connections and counters") {
    val result = stats
    assertEquals(result.partial, false)
    assert(result.uptimeSeconds.exists(_ > 0), s"no uptime: $result")
    assert(result.connections.exists(_.current >= 1), s"no connection: $result")
    assert(result.opcounters.exists(_.command > 0), s"no counters: $result")
  }

  test("a second read has counters that are not smaller") {
    val first  = stats.opcounters.getOrElse(fail("no counters"))
    val second = stats.opcounters.getOrElse(fail("no counters"))
    assert(second.command >= first.command)
    assert(second.insert >= first.insert)
  }

  test("the answer does not tell where the database is") {
    val response = TestAdditions.backend.send(basicRequest.get(uri"${TestServer.serverBaseUrl}/system/stats").auth.bearer(adminBearerToken).response(asString))
    assertEquals(response.code.code, 200)
    val body = response.body.getOrElse("")
    assert(!body.toLowerCase.contains("host"), s"a host in the answer: $body")
    assert("""\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}""".r.findFirstIn(body).isEmpty, s"an address in the answer: $body")
  }

  test("system stats as user is not authorized") {
    val response = executeRequest(systemStatsApi.systemStats("", "", testUserBearerToken, "")())
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message"), Some("user not authorized for request"))
  }

  test("system stats without credentials is not authorized") {
    val response = TestAdditions.backend.send(basicRequest.get(uri"${TestServer.serverBaseUrl}/system/stats").response(ignore))
    assertEquals(response.code.code, 401)
  }
}
