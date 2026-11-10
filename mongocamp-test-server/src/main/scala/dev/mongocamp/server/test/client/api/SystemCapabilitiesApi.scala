package dev.mongocamp.server.test.client.api

import dev.mongocamp.server.test.client.core.ClientCirceSchema
import dev.mongocamp.server.test.client.core.JsonSupport._
import dev.mongocamp.server.test.client.model._
import dev.mongocamp.server.test.TestServer
import sttp.client3._
import sttp.model.Method

object SystemCapabilitiesApi {

  def apply(baseUrl: String = TestServer.serverBaseUrl) = new SystemCapabilitiesApi(baseUrl)
}

/** The capabilities of the server (every logged in user). A class of its own, the generated SystemApi is not edited by hand (see DocumentCountApi). */
class SystemCapabilitiesApi(baseUrl: String) extends ClientCirceSchema {

  def systemCapabilities(username: String, password: String, bearerToken: String, apiKey: String)() =
    basicRequest
      .method(Method.GET, uri"$baseUrl/system/capabilities")
      .contentType("application/json")
      .auth
      .basic(username, password)
      .auth
      .bearer(bearerToken)
      .header("X-AUTH-APIKEY", apiKey)
      .response(asJson[Capabilities])
}
