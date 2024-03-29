/** mongocamp-server No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
  *
  * The version of the OpenAPI document: 1.4.0
  *
  * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech). https://openapi-generator.tech Do not edit the class manually.
  */
package dev.mongocamp.server.test.client.api

import dev.mongocamp.server.converter.CirceSchema
import dev.mongocamp.server.test.TestServer
import dev.mongocamp.server.test.client.core.JsonSupport._
import dev.mongocamp.server.test.client.model.Version
import sttp.client3._
import sttp.model.Method

object InformationApi {

  def apply(baseUrl: String = TestServer.serverBaseUrl) = new InformationApi(baseUrl)
}

class InformationApi(baseUrl: String) extends CirceSchema {

  /** Version Info of the MongoCamp API
    *
    * Expected answers: code 200 : Version () code 0 : ErrorDescription () Headers : x-error-code - Error Code x-error-message - Message of the
    * MongoCampException x-error-additional-info - Additional information for the MongoCampException
    */
  def version(
  ) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/version")
      .contentType("application/json")
      .response(asJson[Version])

}
