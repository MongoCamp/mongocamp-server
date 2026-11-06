package dev.mongocamp.server.test.client.api

import dev.mongocamp.server.test.client.core.ClientCirceSchema
import dev.mongocamp.server.test.client.core.JsonSupport._
import dev.mongocamp.server.test.TestServer
import io.circe.Json
import sttp.client3._
import sttp.model.Method

object IndexCreateApi {

  def apply(baseUrl: String = TestServer.serverBaseUrl) = new IndexCreateApi(baseUrl)
}

/** Create an index with a request body that is given as JSON (orderedKeys and the options of the newer versions). A class of its own, the generated IndexApi
  * and its models are not edited by hand (see DocumentCountApi).
  */
class IndexCreateApi(baseUrl: String) extends ClientCirceSchema {

  def createIndex(username: String, password: String, bearerToken: String, apiKey: String)(collectionName: String, body: Json) =
    basicRequest
      .method(Method.PUT, uri"$baseUrl/mongodb/collections/${collectionName}/index")
      .contentType("application/json")
      .auth
      .basic(username, password)
      .auth
      .bearer(bearerToken)
      .header("X-AUTH-APIKEY", apiKey)
      .body(body.noSpaces)
      .response(asJson[Json])
}
