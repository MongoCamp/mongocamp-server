package dev.mongocamp.server.test.client.api

import dev.mongocamp.server.test.client.core.ClientCirceSchema
import dev.mongocamp.server.test.client.core.JsonSupport._
import dev.mongocamp.server.test.client.model._
import dev.mongocamp.server.test.TestServer
import sttp.client3._
import sttp.model.Method

object IndexDetailsApi {

  def apply(baseUrl: String = TestServer.serverBaseUrl) = new IndexDetailsApi(baseUrl)
}

/** The indices of a Collection with all details. A class of its own, the generated IndexApi is not edited by hand (an edit of it fails an incremental compile,
  * see DocumentCountApi).
  */
class IndexDetailsApi(baseUrl: String) extends ClientCirceSchema {

  def indexDetails(username: String, password: String, bearerToken: String, apiKey: String)(collectionName: String) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/collections/${collectionName}/index/details")
      .contentType("application/json")
      .auth
      .basic(username, password)
      .auth
      .bearer(bearerToken)
      .header("X-AUTH-APIKEY", apiKey)
      .response(asJson[Seq[IndexInfo]])
}
