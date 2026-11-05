package dev.mongocamp.server.test.client.api

import dev.mongocamp.server.test.client.core.ClientCirceSchema
import dev.mongocamp.server.test.client.core.JsonSupport._
import dev.mongocamp.server.test.client.model._
import dev.mongocamp.server.test.TestServer
import sttp.client3._
import sttp.model.Method

object CollectionInfoApi {

  def apply(baseUrl: String = TestServer.serverBaseUrl) = new CollectionInfoApi(baseUrl)
}

/** The numbers of all Collections in one request. A class of its own, the generated CollectionApi is not edited by hand (an edit of it fails an incremental
  * compile, see DocumentCountApi).
  */
class CollectionInfoApi(baseUrl: String) extends ClientCirceSchema {

  def listCollectionInformation(username: String, password: String, bearerToken: String, apiKey: String)(names: Seq[String] = Seq()) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/collections/info?name=$names")
      .contentType("application/json")
      .auth
      .basic(username, password)
      .auth
      .bearer(bearerToken)
      .header("X-AUTH-APIKEY", apiKey)
      .response(asJson[Seq[CollectionSummary]])
}
