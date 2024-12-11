package dev.mongocamp.server.plugin.requestlogging.plugin

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.plugin.requestlogging.client.api.TestEndpointApi
import dev.mongocamp.server.plugin.requestlogging.database.RequestLoggingDatabase
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import org.joda.time.DateTime

import scala.concurrent.duration.{ Duration, DurationInt }
import scala.concurrent.{ Await, ExecutionContext, Future }

class TestLoggingSuite extends MongoCampBaseServerSuite {

  lazy val api: TestEndpointApi = TestEndpointApi()

  test("check a request is logged as 'new' and acknowledge") {
    val testFuture = Future { TestAdditions.backend.send(api.blocking()) }(ExecutionContext.global)
    val startTime = new DateTime()
    var results    = RequestLoggingDatabase.requestLoggingDao.find(Map("methodName" -> "blocking"), Map("date" -> -1)).resultList()
    while (results.isEmpty && startTime.plusSeconds(15).isBeforeNow) {
      Thread.sleep(1.seconds.toMillis / 4)
      results = RequestLoggingDatabase.requestLoggingDao.find(Map("methodName" -> "blocking"), Map("date" -> -1)).resultList()
    }
    assertEquals(results.size, 1)
    assertEquals(results.head.duration == -1, true)
    val testRequestResponse = Await.result(testFuture, Duration.Inf)
    val results2            = RequestLoggingDatabase.requestLoggingDao.find(Map("methodName" -> "blocking"), Map("date" -> -1)).resultList()
    assertEquals(results2.size, 1)
    assertEquals(results.head.requestId, results2.head.requestId)
    assertEquals(results2.head.duration > 0, true)
    assertEquals(testRequestResponse.header("x-request-id").getOrElse("unknown"), results.head.requestId)
  }
}
