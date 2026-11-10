package dev.mongocamp.server.tests

import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.service.ConfigurationService
import dev.mongocamp.server.test.client.api.SystemCapabilitiesApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import dev.mongocamp.server.test.TestServer
import dev.mongocamp.server.BuildInfo
import sttp.client3.basicRequest
import sttp.client3.ignore
import sttp.client3.UriContext

class SystemCapabilitiesSuite extends MongoCampBaseServerSuite {

  lazy val capabilitiesApi: SystemCapabilitiesApi = SystemCapabilitiesApi()

  private def capabilities(token: String) = executeRequestToResponse(capabilitiesApi.systemCapabilities("", "", token, "")())

  test("an admin and a user get the capabilities") {
    List(adminBearerToken, testUserBearerToken).foreach(
      token => {
        val result = capabilities(token)
        assertEquals(result.serverVersion, BuildInfo.version)
        assert(result.mongoDbMajor.exists(_ >= 6), s"MongoDB 6.0 and later is supported: $result")
        List("export", "import", "collectionCopy", "collectionRename", "systemInfo", "capabilities").foreach(
          feature => assert(result.features.contains(feature), s"$feature is missing")
        )
        assertEquals(result.limits.importMaxBytes, 1073741824L)
        assertEquals(result.limits.grantDeniedStatus, 401)
        assert(result.aggregationStagesDenied.contains("$function"), result.aggregationStagesDenied.toString)
        assert(result.aggregationStagesDenied.contains("$out"), result.aggregationStagesDenied.toString)
      }
    )
  }

  test("without credentials the capabilities are not given") {
    val response = TestAdditions.backend.send(basicRequest.get(uri"${TestServer.serverBaseUrl}/system/capabilities").response(ignore))
    assertEquals(response.code.code, 401)
  }

  test("the configuration is in the answer") {
    val statusKey = DefaultConfigurations.ConfigKeyAuthGrantDeniedStatus
    val importKey = DefaultConfigurations.ConfigKeyImportMaxBytes
    val stageKey  = DefaultConfigurations.ConfigKeyAggregationStagesDenied
    val stages    = ConfigurationService.getConfigValue[List[String]](stageKey)
    assertEquals(ConfigurationService.updateConfig(statusKey, 403L), true)
    assertEquals(ConfigurationService.updateConfig(importKey, 2000L), true)
    assertEquals(ConfigurationService.updateConfig(stageKey, List("$limit", "$skip")), true)
    try {
      val result = capabilities(testUserBearerToken)
      assertEquals(result.limits.grantDeniedStatus, 403)
      assertEquals(result.limits.importMaxBytes, 2000L)
      assertEquals(result.aggregationStagesDenied.toList, List("$limit", "$skip"))
    }
    finally {
      ConfigurationService.updateConfig(statusKey, 401L)
      ConfigurationService.updateConfig(importKey, 1073741824L)
      ConfigurationService.updateConfig(stageKey, stages)
    }
    val restored = capabilities(testUserBearerToken)
    assertEquals(restored.limits.grantDeniedStatus, 401)
    assertEquals(restored.limits.importMaxBytes, 1073741824L)
    assertEquals(restored.aggregationStagesDenied.toList, stages.sorted)
  }
}
