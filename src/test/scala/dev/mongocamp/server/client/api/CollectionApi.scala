/** mongocamp No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
  *
  * The version of the OpenAPI document: 0.7.0
  *
  * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech). https://openapi-generator.tech Do not edit the class manually.
  */
package dev.mongocamp.server.client.api

import dev.mongocamp.server.client.core.JsonSupport._
import dev.mongocamp.server.client.model._
import dev.mongocamp.server.converter.CirceSchema
import dev.mongocamp.server.server.TestServer
import sttp.client3._
import sttp.client3.circe.asJson
import sttp.model.Method

object CollectionApi {

  def apply(baseUrl: String = TestServer.serverBaseUrl) = new CollectionApi(baseUrl)
}

class CollectionApi(baseUrl: String) extends CirceSchema {

  /** Aggregate in a given Collection
    *
    * Expected answers: code 200 : Seq[Map] Headers : x-pagination-count-rows - count all elements x-pagination-rows-per-page - Count elements per page
    * x-pagination-current-page - Current page x-pagination-count-pages - Count pages code 400 : String (Invalid value for: body, Invalid value for: query
    * parameter rowsPerPage, Invalid value for: query parameter page) code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of
    * the MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param mongoAggregateRequest
    * @param rowsPerPage
    *   Count elements per page
    * @param page
    *   Requested page of the ResultSets
    */
  def aggregate(apiKey: String, bearerToken: String)(
      collectionName: String,
      mongoAggregateRequest: MongoAggregateRequest,
      rowsPerPage: Option[Long] = None,
      page: Option[Long] = None
  ) = {
    val requestBodyString = encodeAnyToJson(mongoAggregateRequest).toString() // todo: Validate on code generation
    basicRequest
      .method(Method.POST, uri"$baseUrl/mongodb/collections/$collectionName/aggregate?rowsPerPage=$rowsPerPage&page=$page")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .body(requestBodyString)
      .response(asJson[Seq[Map[String, Any]]])
  }

  /** Delete all Document in Collection
    *
    * Expected answers: code 200 : DeleteResponse code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the
    * MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    */
  def clearCollection(apiKey: String, bearerToken: String)(collectionName: String) =
    basicRequest
      .method(Method.DELETE, uri"$baseUrl/mongodb/collections/$collectionName/clear")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[JsonResultBoolean])

  /** Delete a given Collection
    *
    * Expected answers: code 200 : JsonResultBoolean code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the
    * MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    */
  def deleteCollection(apiKey: String, bearerToken: String)(collectionName: String) =
    basicRequest
      .method(Method.DELETE, uri"$baseUrl/mongodb/collections/$collectionName")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[JsonResultBoolean])

  /** Distinct for Field in a given Collection
    *
    * Expected answers: code 200 : Seq[String] Headers : x-pagination-count-rows - count all elements x-pagination-rows-per-page - Count elements per page
    * x-pagination-current-page - Current page x-pagination-count-pages - Count pages code 400 : String (Invalid value for: query parameter rowsPerPage, Invalid
    * value for: query parameter page) code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the MongoCampException
    * x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param field
    *   The field for your distinct Request.
    * @param rowsPerPage
    *   Count elements per page
    * @param page
    *   Requested page of the ResultSets
    */
  def distinct(apiKey: String, bearerToken: String)(
      collectionName: String,
      field: String,
      rowsPerPage: Option[Long] = None,
      page: Option[Long] = None
  ) =
    basicRequest
      .method(Method.POST, uri"$baseUrl/mongodb/collections/$collectionName/distinct/$field?rowsPerPage=$rowsPerPage&page=$page")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[Seq[Any]])

  /** List the Fields in a given collection
    *
    * Expected answers: code 200 : Seq[String] code 400 : String (Invalid value for: query parameter sample size) code 0 : ErrorDescription Headers :
    * x-error-code - Error Code x-error-message - Message of the MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param sampleSize
    *   Use sample size greater 0 (e.g. 1000) for better performance on big collections
    */
  def getCollectionFields(
      apiKey: String,
      bearerToken: String
  )(collectionName: String, sampleSize: Option[Int] = None) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/collections/$collectionName/fields?sample size=$sampleSize")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[Seq[String]])

  /** All Informations about a single Collection
    *
    * Expected answers: code 200 : CollectionStatus code 400 : String (Invalid value for: query parameter includeDetails) code 0 : ErrorDescription Headers :
    * x-error-code - Error Code x-error-message - Message of the MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param includeDetails
    *   Include all details for the Collection
    */
  def getCollectionInformation(
      apiKey: String,
      bearerToken: String
  )(collectionName: String, includeDetails: Option[Boolean] = None) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/collections/$collectionName?includeDetails=$includeDetails")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[CollectionStatus])

  /** List the Fields in a given collection
    *
    * Expected answers: code 200 : JsonSchema code 400 : String (Invalid value for: query parameter sampleSize, Invalid value for: query parameter deepth) code
    * 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the MongoCampException x-error-additional-info - Additional
    * information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http) httpAuth1 (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param sampleSize
    *   Use sample size greater 0 (e.g. 5000) for better performance on big collections
    * @param deepth
    *   How deep should the objects extracted
    */
  def getJsonSchema(apiKey: String, bearerToken: String)(
      collectionName: String,
      sampleSize: Option[Int] = None,
      deepth: Option[Int] = None
  ) =
    {
      basicRequest
        .method(Method.GET, uri"$baseUrl/mongodb/collections/${collectionName}/schema?sampleSize=${sampleSize}&deepth=${deepth}")
        .contentType("application/json")
        .header("X-AUTH-APIKEY", apiKey)
        .auth
        .bearer(bearerToken)
//        .response(asString)
        .response(asJson[JsonSchema])
    }

  /** List the Fields in a given collection
    *
    * Expected answers: code 200 : SchemaAnalysis code 400 : String (Invalid value for: query parameter sampleSize, Invalid value for: query parameter deepth)
    * code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the MongoCampException x-error-additional-info - Additional
    * information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http) httpAuth1 (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param sampleSize
    *   Use sample size greater 0 (e.g. 5000) for better performance on big collections
    * @param deepth
    *   How deep should the objects extracted
    */
  def getSchemaAnalysis(apiKey: String, bearerToken: String)(
      collectionName: String,
      sampleSize: Option[Int] = None,
      deepth: Option[Int] = None
  ) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/collections/${collectionName}/schema/analysis?sampleSize=${sampleSize}&deepth=${deepth}")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[SchemaAnalysis])

  /** List of all Collections of the default database
    *
    * Expected answers: code 200 : Seq[String] code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the MongoCampException
    * x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http) httpAuth1 (http)
    */
  def listCollections(apiKey: String, bearerToken: String)(
  ) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/collections")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[Seq[String]])

  /** List of all Collections of the given database
    *
    * Expected answers: code 200 : Seq[String] code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the MongoCampException
    * x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http) httpAuth1 (http)
    *
    * @param databaseName
    *   Name of your Database
    */
  def listCollectionsByDatabase(apiKey: String, bearerToken: String)(databaseName: String) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/databases/${databaseName}/collections")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[Seq[String]])

}
