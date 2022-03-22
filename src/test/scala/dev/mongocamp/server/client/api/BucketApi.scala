/** mongocamp No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
  *
  * The version of the OpenAPI document: 0.8.1-SNAPSHOT
  *
  * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech). https://openapi-generator.tech Do not edit the class manually.
  */
package dev.mongocamp.server.client.api

import dev.mongocamp.server.client.core.JsonSupport._
import dev.mongocamp.server.client.model.{ BucketInformation, JsonResultBoolean }
import dev.mongocamp.server.converter.CirceSchema
import dev.mongocamp.server.server.TestServer
import sttp.client3._
import sttp.client3.circe.asJson
import sttp.model.Method

object BucketApi {

  def apply(baseUrl: String = TestServer.serverBaseUrl) = new BucketApi(baseUrl)
}

class BucketApi(baseUrl: String) extends CirceSchema {

  /** Delete all Files in Bucket
    *
    * Expected answers: code 200 : JsonResultBoolean code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the
    * MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param bucketName
    *   The name of your MongoDb Collection
    */
  def clearBucket(apiKey: String, bearerToken: String)(bucketName: String) = {
    basicRequest
      .method(Method.DELETE, uri"$baseUrl/mongodb/buckets/$bucketName/clear")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[JsonResultBoolean])
  }

  /** Delete a given Bucket
    *
    * Expected answers: code 200 : JsonResultBoolean code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the
    * MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param bucketName
    *   The name of your MongoDb Collection
    */
  def deleteBucket(apiKey: String, bearerToken: String)(bucketName: String) = {
    basicRequest
      .method(Method.DELETE, uri"$baseUrl/mongodb/buckets/$bucketName")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[JsonResultBoolean])
  }

  /** All Information about a single Bucket
    *
    * Expected answers: code 200 : BucketInformation code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the
    * MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param bucketName
    *   The name of your MongoDb Collection
    */
  def getBucket(apiKey: String, bearerToken: String)(bucketName: String) = {
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/buckets/$bucketName")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[BucketInformation])
  }

  /** List of all Buckets of the default database
    *
    * Expected answers: code 200 : Seq[String] code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the MongoCampException
    * x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    */
  def listBuckets(apiKey: String, bearerToken: String)() = {
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/buckets")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[Seq[String]])
  }

}