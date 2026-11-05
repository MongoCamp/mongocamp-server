package dev.mongocamp.server.test.client.api

import dev.mongocamp.server.test.client.core.ClientCirceSchema
import dev.mongocamp.server.test.client.core.JsonSupport._
import dev.mongocamp.server.test.client.model._
import dev.mongocamp.server.test.TestServer
import sttp.client3._
import sttp.model.Method

object CollectionAggregateFilterApi {

  def apply(baseUrl: String = TestServer.serverBaseUrl) = new CollectionAggregateFilterApi(baseUrl)
}

/** Aggregate with a Lucene filter, applied as the first stage of the pipeline. A class of its own, the generated CollectionApi is not edited by hand (an edit
  * of it fails an incremental compile, see DocumentCountApi).
  */
class CollectionAggregateFilterApi(baseUrl: String) extends ClientCirceSchema {

  def aggregate(username: String, password: String, bearerToken: String, apiKey: String)(
    collectionName: String,
    mongoAggregateRequest: MongoAggregateRequest,
    filter: Option[String] = None,
    rowsPerPage: Option[Long] = None,
    page: Option[Long] = None
  ) = {
    val requestBodyString = encodeAnyToJson(mongoAggregateRequest).toString()
    basicRequest
      .method(Method.POST, uri"$baseUrl/mongodb/collections/${collectionName}/aggregate?rowsPerPage=${rowsPerPage}&page=${page}&filter=${filter}")
      .contentType("application/json")
      .auth
      .basic(username, password)
      .auth
      .bearer(bearerToken)
      .header("X-AUTH-APIKEY", apiKey)
      .body(requestBodyString)
      .response(asJson[Seq[Map[String, Any]]])
  }
}
