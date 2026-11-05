package dev.mongocamp.server.test.client.api

import dev.mongocamp.server.test.client.core.ClientCirceSchema
import dev.mongocamp.server.test.client.core.JsonSupport._
import dev.mongocamp.server.test.client.model._
import dev.mongocamp.server.test.TestServer
import sttp.client3._
import sttp.model.Method

object DocumentInsertManyApi {

  def apply(baseUrl: String = TestServer.serverBaseUrl) = new DocumentInsertManyApi(baseUrl)
}

/** Insert many Documents with the option `ordered`. A class of its own, the generated DocumentApi is not edited by hand (an edit of it fails an incremental
  * compile, see DocumentCountApi).
  */
class DocumentInsertManyApi(baseUrl: String) extends ClientCirceSchema {

  def insertMany(username: String, password: String, bearerToken: String, apiKey: String)(
    collectionName: String,
    requestBody: Seq[Map[String, Any]],
    ordered: Option[Boolean] = None
  ) =
    basicRequest
      .method(Method.PUT, uri"$baseUrl/mongodb/collections/${collectionName}/documents/many/insert?ordered=${ordered}")
      .contentType("application/json")
      .auth
      .basic(username, password)
      .auth
      .bearer(bearerToken)
      .header("X-AUTH-APIKEY", apiKey)
      .body(encodeAnyToJson(requestBody).toString())
      .response(asJson[InsertManyResponse])
}
