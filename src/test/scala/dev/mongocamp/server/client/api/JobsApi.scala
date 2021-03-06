/** mongocamp-server No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
  *
  * The version of the OpenAPI document: 1.1.2-SNAPSHOT
  *
  * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech). https://openapi-generator.tech Do not edit the class manually.
  */
package dev.mongocamp.server.client.api

import dev.mongocamp.server.client.core.JsonSupport._
import dev.mongocamp.server.client.model.{JobConfig, JobInformation, JsonResultBoolean}
import dev.mongocamp.server.server.TestServer
import sttp.client3._
import sttp.client3.circe.asJson
import sttp.model.Method

object JobsApi {
  def apply(baseUrl: String = TestServer.serverBaseUrl) = new JobsApi(baseUrl)
}

class JobsApi(baseUrl: String) {

  /** Add Job and get JobInformation back
    *
    * Expected answers: code 200 : JobInformation code 400 : String (Invalid value for: body) code 0 : ErrorDescription Headers : x-error-code - Error Code
    * x-error-message - Message of the MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http) httpAuth1 (http)
    *
    * @param jobGroup
    *   Group Name of the Job
    * @param jobName
    *   Name of the Job
    * @param jobConfig
    */
  def addJob(apiKey: String, bearerToken: String, username: String, password: String)(jobGroup: String, jobName: String, jobConfig: JobConfig) = {
    basicRequest
      .method(Method.PATCH, uri"$baseUrl/system/jobs/${jobGroup}/${jobName}")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .auth
      .basic(username, password)
      .body(jobConfig)
      .response(asJson[JobInformation])
  }

  /** Delete Job and reload all Job Information
    *
    * Expected answers: code 200 : JsonResultBoolean code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the
    * MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http) httpAuth1 (http)
    *
    * @param jobGroup
    *   Group Name of the Job
    * @param jobName
    *   Name of the Job
    */
  def deleteJob(apiKey: String, bearerToken: String, username: String, password: String)(jobGroup: String, jobName: String) = {
    basicRequest
      .method(Method.DELETE, uri"$baseUrl/system/jobs/${jobGroup}/${jobName}")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .auth
      .basic(username, password)
      .response(asJson[JsonResultBoolean])
  }

  /** Execute scheduled Job manually
    *
    * Expected answers: code 200 : JsonResultBoolean code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the
    * MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http) httpAuth1 (http)
    *
    * @param jobGroup
    *   Group Name of the Job
    * @param jobName
    *   Name of the Job
    */
  def executeJob(apiKey: String, bearerToken: String, username: String, password: String)(jobGroup: String, jobName: String) = {
    basicRequest
      .method(Method.POST, uri"$baseUrl/system/jobs/${jobGroup}/${jobName}")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .auth
      .basic(username, password)
      .response(asJson[JsonResultBoolean])
  }

  /** Returns the List of all registered Jobs with full information
    *
    * Expected answers: code 200 : Seq[JobInformation] code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the
    * MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http) httpAuth1 (http)
    */
  def jobsList(apiKey: String, bearerToken: String, username: String, password: String)() =
    basicRequest
      .method(Method.GET, uri"$baseUrl/system/jobs")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .auth
      .basic(username, password)
      .response(asJson[Seq[JobInformation]])

  /** Returns the List of possible job classes
    *
    * Expected answers: code 200 : Seq[String] code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the MongoCampException
    * x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http) httpAuth1 (http)
    */
  def possibleJobsList(apiKey: String, bearerToken: String, username: String, password: String)() = {
    basicRequest
      .method(Method.GET, uri"$baseUrl/system/jobs/classes")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .auth
      .basic(username, password)
      .response(asJson[Seq[String]])
  }

  /** Register an Job and return the JobInformation with next schedule information
    *
    * Expected answers: code 200 : JobInformation code 400 : String (Invalid value for: body) code 0 : ErrorDescription Headers : x-error-code - Error Code
    * x-error-message - Message of the MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http) httpAuth1 (http)
    *
    * @param jobConfig
    */
  def registerJob(apiKey: String, bearerToken: String, username: String, password: String)(jobConfig: JobConfig) = {
    basicRequest
      .method(Method.PUT, uri"$baseUrl/system/jobs")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .auth
      .basic(username, password)
      .body(jobConfig)
      .response(asJson[JobInformation])
  }

  /** Add Job and get JobInformation back
    *
    * Expected answers: code 200 : JobInformation code 400 : String (Invalid value for: body) code 0 : ErrorDescription Headers : x-error-code - Error Code
    * x-error-message - Message of the MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http) httpAuth1 (http)
    *
    * @param jobGroup
    *   Group Name of the Job
    * @param jobName
    *   Name of the Job
    * @param jobConfig
    */
  def updateJob(apiKey: String, bearerToken: String, username: String, password: String)(jobGroup: String, jobName: String, jobConfig: JobConfig) = {
    basicRequest
      .method(Method.PATCH, uri"$baseUrl/system/jobs/${jobGroup}/${jobName}")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .auth
      .basic(username, password)
      .body(jobConfig)
      .response(asJson[JobInformation])
  }

}
