/** mongocamp No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
  *
  * The version of the OpenAPI document: 0.5.0
  *
  * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech). https://openapi-generator.tech Do not edit the class manually.
  */
package com.quadstingray.mongo.camp.client.api

import com.quadstingray.mongo.camp.client.core.JsonSupport._
import com.quadstingray.mongo.camp.client.model._
import com.quadstingray.mongo.camp.converter.CirceSchema
import sttp.client3._
import sttp.client3.circe.asJson
import sttp.model.Method

object DocumentsApi {
  def apply(baseUrl: String = com.quadstingray.mongo.camp.server.TestServer.serverBaseUrl) = new DocumentsApi(baseUrl)
}
class DocumentsApi(baseUrl: String) extends CirceSchema {

  /** Delete one Document from Collection
    *
    * Expected answers: code 200 : DeleteResponse code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the
    * MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param documentId
    *   DocumentId to delete
    */
  def deleteDocument(apiKey: String, bearerToken: String)(collectionName: String, documentId: String) =
    basicRequest
      .method(Method.DELETE, uri"$baseUrl/mongodb/collections/$collectionName/documents/$documentId")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[DeleteResponse])

  /** Delete many Document in Collection
    *
    * Expected answers: code 200 : DeleteResponse code 400 : String (Invalid value for: body) code 0 : ErrorDescription Headers : x-error-code - Error Code
    * x-error-message - Message of the MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param requestBody
    */
  def deleteMany(apiKey: String, bearerToken: String)(collectionName: String, requestBody: Map[String, Any]) =
    basicRequest
      .method(Method.DELETE, uri"$baseUrl/mongodb/collections/$collectionName/documents/many/delete")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .body(requestBody)
      .response(asJson[DeleteResponse])

  /** Get Documents paginated from MongoDatabase Collection
    *
    * Expected answers: code 200 : Seq[Map[String, Any]] Headers : x-pagination-count-rows - count all elements x-pagination-rows-per-page - Count elements per
    * page x-pagination-current-page - Current page x-pagination-count-pages - Count pages code 400 : String (Invalid value for: query parameter filter, Invalid
    * value for: query parameter sort, Invalid value for: query parameter projection, Invalid value for: query parameter rowsPerPage, Invalid value for: query
    * parameter page) code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the MongoCampException x-error-additional-info
    *   - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param filter
    *   MongoDB Filter Query by Default all filter
    * @param sort
    *   MongoDB sorting
    * @param projection
    *   MongoDB projection
    * @param rowsPerPage
    *   Count elements per page
    * @param page
    *   Requested page of the ResultSets
    */
  def documentsList(apiKey: String, bearerToken: String)(
      collectionName: String,
      filter: Option[String] = None,
      sort: Option[String] = None,
      projection: Option[String] = None,
      rowsPerPage: Option[Long] = None,
      page: Option[Long] = None
  ) =
    basicRequest
      .method(
        Method.GET,
        uri"$baseUrl/mongodb/collections/$collectionName/documents?filter=$filter&sort=$sort&projection=$projection&rowsPerPage=$rowsPerPage&page=$page"
      )
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[Seq[Map[String, Any]]])

  /** Alternative to GET Route for more complex queries and URL max. Length
    *
    * Expected answers: code 200 : Seq[Map[String, Any]] Headers : x-pagination-count-rows - count all elements x-pagination-rows-per-page - Count elements per
    * page x-pagination-current-page - Current page x-pagination-count-pages - Count pages code 400 : String (Invalid value for: body, Invalid value for: query
    * parameter rowsPerPage, Invalid value for: query parameter page) code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of
    * the MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param mongoFindRequest
    *   @param rowsPerPage Count elements per page
    * @param page
    *   Requested page of the ResultSets
    */
  def find(
      apiKey: String,
      bearerToken: String
  )(collectionName: String, mongoFindRequest: MongoFindRequest, rowsPerPage: Option[Long] = None, page: Option[Long] = None) =
    basicRequest
      .method(Method.POST, uri"$baseUrl/mongodb/collections/$collectionName/documents?rowsPerPage=$rowsPerPage&page=$page")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .body(mongoFindRequest)
      .response(asJson[Seq[Map[String, Any]]])

  /** Get one Document from Collection
    *
    * Expected answers: code 200 : [Map[String, Any]] code 0 : ErrorDescription Headers : x-error-code - Error Code x-error-message - Message of the
    * MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param documentId
    *   DocumentId to read
    */
  def getDocument(apiKey: String, bearerToken: String)(collectionName: String, documentId: String) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/collections/$collectionName/documents/$documentId")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .response(asJson[Map[String, Any]])

  /** Insert one Document in Collection
    *
    * Expected answers: code 200 : InsertResponse code 400 : String (Invalid value for: body) code 0 : ErrorDescription Headers : x-error-code - Error Code
    * x-error-message - Message of the MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param requestBody
    *   JSON Representation for your Document.
    */
  def insert(apiKey: String, bearerToken: String)(collectionName: String, requestBody: Map[String, Any]) =
    basicRequest
      .method(Method.PUT, uri"$baseUrl/mongodb/collections/$collectionName/documents")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .body(requestBody)
      .response(asJson[InsertResponse])

  /** Insert many documents in Collection
    *
    * Expected answers: code 200 : InsertResponse code 400 : String (Invalid value for: body) code 0 : ErrorDescription Headers : x-error-code - Error Code
    * x-error-message - Message of the MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param requestBody
    */
  def insertMany(apiKey: String, bearerToken: String)(collectionName: String, requestBody: Seq[Map[String, Any]]) =
    basicRequest
      .method(Method.PUT, uri"$baseUrl/mongodb/collections/$collectionName/documents/many/insert")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .body(requestBody)
      .response(asJson[InsertResponse])

  /** 'Replace' one Document with the new document from Request in Collection
    *
    * Expected answers: code 200 : UpdateResponse code 400 : String (Invalid value for: body) code 0 : ErrorDescription Headers : x-error-code - Error Code
    * x-error-message - Message of the MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param documentId
    *   DocumentId to update
    * @param requestBody
    */
  def updateDocument(apiKey: String, bearerToken: String)(collectionName: String, documentId: String, requestBody: Map[String, Any]) =
    basicRequest
      .method(Method.PATCH, uri"$baseUrl/mongodb/collections/$collectionName/documents/$documentId")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .body(requestBody)
      .response(asJson[UpdateResponse])

  /** Update the document Parts with the values from the Request
    *
    * Expected answers: code 200 : UpdateResponse code 400 : String (Invalid value for: body) code 0 : ErrorDescription Headers : x-error-code - Error Code
    * x-error-message - Message of the MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param documentId
    *   DocumentId to update
    * @param requestBody
    */
  def updateDocumentPartitial(apiKey: String, bearerToken: String)(collectionName: String, documentId: String, requestBody: Map[String, Any]) =
    basicRequest
      .method(Method.PATCH, uri"$baseUrl/mongodb/collections/$collectionName/documents/$documentId/partitial")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .body(requestBody)
      .response(asJson[UpdateResponse])

  /** Update many Document in Collection
    *
    * Expected answers: code 200 : UpdateResponse code 400 : String (Invalid value for: body) code 0 : ErrorDescription Headers : x-error-code - Error Code
    * x-error-message - Message of the MongoCampException x-error-additional-info - Additional information for the MongoCampException
    *
    * Available security schemes: apiKeyAuth (apiKey) httpAuth (http)
    *
    * @param collectionName
    *   The name of your MongoDb Collection
    * @param updateRequest
    */
  def updateMany(apiKey: String, bearerToken: String)(collectionName: String, updateRequest: UpdateRequest) =
    basicRequest
      .method(Method.PATCH, uri"$baseUrl/mongodb/collections/$collectionName/documents/many/update")
      .contentType("application/json")
      .header("X-AUTH-APIKEY", apiKey)
      .auth
      .bearer(bearerToken)
      .body(updateRequest)
      .response(asJson[UpdateResponse])

}
