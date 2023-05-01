package dev.mongocamp.server.tests

import dev.mongocamp.server.test.client.api.ApplicationApi
import dev.mongocamp.server.test.client.model.JsonValueAny

class ApplicationSuite extends BaseServerSuite {

  val applicationApi: ApplicationApi = ApplicationApi()

  test("Show all System Settings") {
    val systemSettings = executeRequestToResponse(applicationApi.settings("", "", adminBearerToken, "")())
    assertEquals(systemSettings.filePlugins.size, 1)
    assertEquals(systemSettings.routesPlugins.size, 7)
    assertEquals(systemSettings.ignoredPlugins.size, 0)
    assertEquals(systemSettings.configurations.size, 37)
    assertEquals(systemSettings.configurations("CORS_HEADERS_ALLOWED"), List("Authorization", "Content-Type", "X-Requested-With", "X-AUTH-APIKEY"))
  }

  test("List Configurations for Application") {
    val configurationsList = executeRequestToResponse(applicationApi.listConfigurations("", "", adminBearerToken, "")())
    assertEquals(configurationsList.size, 37)
    val config = configurationsList.find(_.key.equalsIgnoreCase("SERVER_INTERFACE")).get
    assertEquals(config.configType, "String")
    assertEquals(config.key, "SERVER_INTERFACE")
    assertEquals(config.value, "0.0.0.0")
    assertEquals(config.needsRestartForActivation, true)
  }

  test("Get Configuration for key") {
    val config = executeRequestToResponse(applicationApi.getConfig("", "", adminBearerToken, "")("SERVER_INTERFACE"))
    assertEquals(config.configType, "String")
    assertEquals(config.key, "SERVER_INTERFACE")
    assertEquals(config.value, "0.0.0.0")
    assertEquals(config.needsRestartForActivation, true)
  }

  test("Edit Configuration for Key") {
    val eventMetrics = executeRequestToResponse(applicationApi.updateConfiguration("", "", adminBearerToken, "")("SERVER_INTERFACE", JsonValueAny("localhost")))
    val config       = executeRequestToResponse(applicationApi.getConfig("", "", adminBearerToken, "")("SERVER_INTERFACE"))
    assertEquals(config.configType, "String")
    assertEquals(config.key, "SERVER_INTERFACE")
    assertEquals(config.value, "localhost")
    assertEquals(config.needsRestartForActivation, true)
  }
}
