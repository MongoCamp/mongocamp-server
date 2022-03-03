/** mongocamp No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
  *
  * The version of the OpenAPI document: 0.5.0
  *
  * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech). https://openapi-generator.tech Do not edit the class manually.
  */
package com.quadstingray.mongo.camp.client.api

import com.quadstingray.mongo.camp.client.core.JsonSupport._
import com.quadstingray.mongo.camp.client.model.{ DatabaseInfo, JsonResultBoolean }
import com.quadstingray.mongo.camp.converter.CirceSchema
import sttp.client3._
import sttp.client3.circe.asJson
import sttp.model.Method

object DatabaseApi {
  def apply(baseUrl: String = com.quadstingray.mongo.camp.server.TestServer.serverBaseUrl) = new DatabaseApi(baseUrl)
}

class DatabaseApi(baseUrl: String) extends CirceSchema {

  /** List of all Databases Infos
    *
    * Expected answers: code 200 : Seq[DatabaseInfo] code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the
    * MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    */
  def databaseInfos(apiKey: String, bearerToken: String)(
  ) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/databases/infos")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[Seq[DatabaseInfo]])

  /** List of all Databases
    *
    * Expected answers: code 200 : Seq[String] code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the MongoCampException
    * x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    */
  def databaseList(apiKey: String, bearerToken: String)(
  ) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/databases")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[Seq[String]])

  /** All Informations about one Database
    *
    * Expected answers: code 200 : JsonResultBoolean code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the
    * MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param databaseName
    *   Name of your Database
    */
  def deleteDatabase(apiKey: String, bearerToken: String)(databaseName: String) =
    basicRequest
      .method(Method.DELETE, uri"$baseUrl/mongodb/databases/$databaseName")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[JsonResultBoolean])

  /** All Informations about one Database
    *
    * Expected answers: code 200 : DatabaseInfo code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the
    * MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param databaseName
    *   Name of your Database
    */
  def getDatabaseInfo(apiKey: String, bearerToken: String)(databaseName: String) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/databases/$databaseName")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[DatabaseInfo])

}