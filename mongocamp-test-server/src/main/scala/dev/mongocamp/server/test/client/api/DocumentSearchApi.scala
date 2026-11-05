package dev.mongocamp.server.test.client.api

import dev.mongocamp.server.test.client.core.ClientCirceSchema
import dev.mongocamp.server.test.client.core.JsonSupport._
import dev.mongocamp.server.test.client.model._
import dev.mongocamp.server.test.TestServer
import io.circe.Json
import sttp.client3._
import sttp.model.Method

object DocumentSearchApi {

  def apply(baseUrl: String = TestServer.serverBaseUrl) = new DocumentSearchApi(baseUrl)
}

/** The list of documents with the parameters in the request body. A class of its own, the generated DocumentApi is not edited by hand (an edit of it fails an
  * incremental compile, see DocumentCountApi).
  */
class DocumentSearchApi(baseUrl: String) extends ClientCirceSchema {

  /** The list of documents with the filter (Lucene), sort and projection in the request body.
    *
    * Available security schemes: httpAuth1 (http) httpAuth (http) apiKeyAuth (apiKey)
    */
  def searchDocuments(
    username: String,
    password: String,
    bearerToken: String,
    apiKey: String
  )(collectionName: String, searchRequest: SearchRequest, rowsPerPage: Option[Long] = None, page: Option[Long] = None) = {
    // the body is built by hand: None must not be sent as {}
    val body = Json
      .obj(
        List(
          searchRequest.filter.map(
            f => "filter" -> Json.fromString(f)
          ),
          Some("sort"       -> Json.arr(searchRequest.sort.map(Json.fromString): _*)),
          Some("projection" -> Json.arr(searchRequest.projection.map(Json.fromString): _*))
        ).flatten: _*
      )
      .noSpaces
    basicRequest
      .method(Method.POST, uri"$baseUrl/mongodb/collections/${collectionName}/documents/search?rowsPerPage=${rowsPerPage}&page=${page}")
      .contentType("application/json")
      .auth
      .basic(username, password)
      .auth
      .bearer(bearerToken)
      .header("X-AUTH-APIKEY", apiKey)
      .body(body)
      .response(asJson[Seq[Map[String, Any]]])
  }
}
