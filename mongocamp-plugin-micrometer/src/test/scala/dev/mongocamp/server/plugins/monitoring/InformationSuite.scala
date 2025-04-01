package dev.mongocamp.server.plugins.monitoring

import dev.mongocamp.server.BuildInfo
import dev.mongocamp.server.test.client.api.SystemApi
import org.joda.time.DateTime

class InformationSuite extends MicrometerBaseServerSuite {

  lazy val informationApi: SystemApi = SystemApi()

  test("check version by api request") {
    val version = executeRequestToResponse(informationApi.version())
    assertEquals(version.name, BuildInfo.name)
    assertEquals(version.version, BuildInfo.version)
    assertEquals(version.builtAt, new DateTime(BuildInfo.builtAtMillis))
  }

}
