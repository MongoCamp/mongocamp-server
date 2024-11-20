package dev.mongocamp.server.plugins.monitoring

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.client.api.MetricsApi
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.test.client.api.SystemApi
import org.joda.time.DateTime
class PersistenceSuite extends MicrometerBaseServerSuite {

  lazy val metricsApi: MetricsApi = MetricsApi()
  lazy val informationApi: SystemApi = SystemApi()

  override def beforeEach(context: BeforeEach): Unit = {
    val jvmMetrics = executeRequestToResponse(metricsApi.jvmMetrics("", "", adminBearerToken, "")())
    val systemMetrics = executeRequestToResponse(metricsApi.systemMetrics("", "", adminBearerToken, "")())
    val mongoDbMetrics = executeRequestToResponse(metricsApi.mongoDbMetrics("", "", adminBearerToken, "")())
    val eventMetrics = executeRequestToResponse(metricsApi.eventMetrics("", "", adminBearerToken, "")())
    val version = executeRequestToResponse(informationApi.version())
    val startTime = new DateTime()
    while (startTime.plusSeconds(15).isBeforeNow) {
      // wait for collect metrics
    }
  }

  test("Check JVM Metrics at Database") {
    val dbResponse = MongoDatabase.databaseProvider.dao("monitoring_jvm").find(sort = Map("date" -> -1)).results()
    assertEquals(dbResponse.nonEmpty, true)
    val document = dbResponse.head
    assertEquals(document.getDoubleValue("jvm_threads_live.value") > 50.0, true)
    assertEquals(document.getStringValue("jvm_buffer_memory_used.metricType"), "gauge")
  }

  test("Check System Metrics at Database") {
    val dbResponse = MongoDatabase.databaseProvider.dao("monitoring_system").find(sort = Map("date" -> -1)).results()
    assertEquals(dbResponse.nonEmpty, true)
    val document = dbResponse.head
    assertEquals(document.getDoubleValue("process_files_open.value") > 0.0, true)
    assertEquals(document.getDoubleValue("process_files_open.value") > 5.0, true)
    assertEquals(document.getStringValue("process_cpu_usage.metricType"), "gauge")
    assertEquals(document.getStringValue("disk_total.metricType"), "gauge")
  }

  test("Check MongoDb Metrics at Database") {
    val dbResponse = MongoDatabase.databaseProvider.dao("monitoring_mongo_db").find(sort = Map("date" -> -1)).results()
    assertEquals(dbResponse.nonEmpty, true)
    val document = dbResponse.head
    assertEquals(document.getDoubleValue("mongodb_server_status_operations_query.value") > 50.0, true)
    assertEquals(document.getStringValue("mongodb_server_status_operations_query.metricType"), "gauge")
    assertEquals(document.getStringValue("mongodb_collection_test_mc_users_avgObjSize.metricType"), "gauge")
    assertEquals(document.getStringValue("mongodb_driver_pool_size.metricType"), "gauge")
    assertEquals(document.getDoubleValue("mongodb_driver_pool_size.value") >= 1.0, true)
    assertEquals(document.getStringValue("mongodb_driver_commands.metricType"), "timer")
  }

  test("Check Event Metrics at Database") {
    val dbResponse = MongoDatabase.databaseProvider.dao("monitoring_event").find(sort = Map("date" -> -1)).results()
    assertEquals(dbResponse.nonEmpty, true)
    val document = dbResponse.head
    assertEquals(document.getStringValue("event_http_http_request_start.metricType"), "distribution_summary")
    assertEquals(document.getStringValue("event_http_http_request_completed.metricType"), "timer")
  }

}
