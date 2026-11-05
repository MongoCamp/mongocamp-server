package dev.mongocamp.server.test.client.api

import dev.mongocamp.server.test.client.core.ClientCirceSchema
import dev.mongocamp.server.test.client.core.JsonSupport._
import dev.mongocamp.server.test.client.model._
import dev.mongocamp.server.test.TestServer
import sttp.client3._
import sttp.model.Method

object IndexStatsApi {

  def apply(baseUrl: String = TestServer.serverBaseUrl) = new IndexStatsApi(baseUrl)
}

/** How often the indices of a Collection were used. A class of its own, the generated IndexApi is not edited by hand (an edit of it fails an incremental
  * compile, see DocumentCountApi).
  */
class IndexStatsApi(baseUrl: String) extends ClientCirceSchema {

  /** How often every Index of the given Collection was used since the counting started.
    *
    * Available security schemes: httpAuth1 (http) httpAuth (http) apiKeyAuth (apiKey)
    */
  def indexStats(username: String, password: String, bearerToken: String, apiKey: String)(collectionName: String) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/collections/${collectionName}/index/stats")
      .contentType("application/json")
      .auth
      .basic(username, password)
      .auth
      .bearer(bearerToken)
      .header("X-AUTH-APIKEY", apiKey)
      .response(asJson[Seq[IndexUsage]])
}
