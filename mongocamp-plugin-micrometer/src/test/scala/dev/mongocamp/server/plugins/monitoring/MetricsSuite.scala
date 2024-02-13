package dev.mongocamp.server.plugins.monitoring

import dev.mongocamp.server.client.api.MetricsApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import dev.mongocamp.server.test.client.api.ApplicationApi

class MetricsSuite extends MicrometerBaseServerSuite {

  lazy val applicationApi: ApplicationApi = ApplicationApi()
  lazy val metricsApi: MetricsApi = MetricsApi()

  test("Show all JVM Metrics") {
    val jvmMetrics = executeRequestToResponse(metricsApi.jvmMetrics("", "", adminBearerToken, "")())
    assertEquals(jvmMetrics.size > 40, true)
  }

  test("Show all System Metrics") {
    val systemMetrics = executeRequestToResponse(metricsApi.systemMetrics("", "", adminBearerToken, "")())
    assertEquals(systemMetrics.size, 10)
  }

  test("Show all MongoDb Metrics") {
    val mongoDbMetrics = executeRequestToResponse(metricsApi.mongoDbMetrics("", "", adminBearerToken, "")())
    assertEquals(mongoDbMetrics.size > 25, true)
  }

  test("Show all Event Metrics") {
    val eventMetrics = executeRequestToResponse(metricsApi.eventMetrics("", "", adminBearerToken, "")())
    assert(eventMetrics.size > 5, "More than 10 event metrics")
  }

}
