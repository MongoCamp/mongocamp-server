package dev.mongocamp.server.test.client.api

import dev.mongocamp.server.test.client.core.ClientCirceSchema
import dev.mongocamp.server.test.client.core.JsonSupport._
import dev.mongocamp.server.test.client.model._
import dev.mongocamp.server.test.TestServer
import sttp.client3._
import sttp.model.Method

object DocumentCountApi {

  def apply(baseUrl: String = TestServer.serverBaseUrl) = new DocumentCountApi(baseUrl)
}

/** Count the Documents of a Collection. Kept out of the generated DocumentApi: when sbt recompiles only that file (incremental build) the implicit search of
  * the body serializer of its `insertMany` fails (Scala 3), a full compile (`mongocamp-test-server/clean`) works. A new class does not touch that file.
  */
class DocumentCountApi(baseUrl: String) extends ClientCirceSchema {

  /** Count the Documents of the given Collection that match the filter without loading them
    *
    * Available security schemes: httpAuth1 (http) httpAuth (http) apiKeyAuth (apiKey)
    *
    * @param collectionName
    *   Name of the Collection
    * @param filter
    *   Filter Query like on the list of documents, by default all documents are counted
    */
  def countDocuments(username: String, password: String, bearerToken: String, apiKey: String)(collectionName: String, filter: Option[String] = None) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/collections/${collectionName}/documents/count?filter=${filter}")
      .contentType("application/json")
      .auth
      .basic(username, password)
      .auth
      .bearer(bearerToken)
      .header("X-AUTH-APIKEY", apiKey)
      .response(asJson[CountResponse])
}
