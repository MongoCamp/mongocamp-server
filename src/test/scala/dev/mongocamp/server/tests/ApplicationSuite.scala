package dev.mongocamp.server.tests

import dev.mongocamp.server.client.api.ApplicationApi

class ApplicationSuite extends BaseSuite {

  val applicationApi: ApplicationApi = ApplicationApi()

  test("Show all System Settings") {
    val systemSettings = executeRequestToResponse(applicationApi.settings("", adminBearerToken)())
    assertEquals(systemSettings.filePlugins.size, 1)
    assertEquals(systemSettings.routesPlugins.size, 7)
    assertEquals(systemSettings.ignoredPlugins.size, 0)
    assertEquals(systemSettings.configurations.size, 31)
    assertEquals(systemSettings.configurations("CORS_HEADERS_ALLOWED"), List("Authorization", "Content-Type", "X-Requested-With", "X-AUTH-APIKEY"))
  }

  test("Show all JVM Metrics") {
    val jvmMetrics = executeRequestToResponse(applicationApi.jvmMetrics("", adminBearerToken)())
    assertEquals(jvmMetrics.size > 40, true)
  }

  test("Show all System Metrics") {
    val systemMetrics = executeRequestToResponse(applicationApi.systemMetrics("", adminBearerToken)())
    assertEquals(systemMetrics.size, 10)
  }

  test("Show all MongoDb Metrics") {
    val mongoDbMetrics = executeRequestToResponse(applicationApi.mongoDbMetrics("", adminBearerToken)())
    assertEquals(mongoDbMetrics.size > 25, true)
  }

  test("Show all Event Metrics") {
    val eventMetrics = executeRequestToResponse(applicationApi.eventMetrics("", adminBearerToken)())
    assert(eventMetrics.size > 5, "More than 10 event metrics")
  }

}
