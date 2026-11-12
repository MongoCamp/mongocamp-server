package dev.mongocamp.server.test.client.api

import dev.mongocamp.server.test.client.core.ClientCirceSchema
import dev.mongocamp.server.test.client.core.JsonSupport._
import dev.mongocamp.server.test.TestServer
import io.circe.Json
import sttp.client3._
import sttp.model.Method

object CollectionOptionsApi {

  def apply(baseUrl: String = TestServer.serverBaseUrl) = new CollectionOptionsApi(baseUrl)
}

/** Create a Collection with options, read and change them. A class of its own, the generated CollectionApi is not edited by hand (see DocumentCountApi). */
class CollectionOptionsApi(baseUrl: String) extends ClientCirceSchema {

  def createCollection(username: String, password: String, bearerToken: String, apiKey: String)(collectionName: String, body: Option[Json] = None) = {
    val request = basicRequest
      .method(Method.PUT, uri"$baseUrl/mongodb/collections/${collectionName}")
      .contentType("application/json")
      .auth
      .basic(username, password)
      .auth
      .bearer(bearerToken)
      .header("X-AUTH-APIKEY", apiKey)
    body.fold(request)(json => request.body(json.noSpaces)).response(asJson[Json])
  }

  def getCollectionOptions(username: String, password: String, bearerToken: String, apiKey: String)(collectionName: String) =
    basicRequest
      .method(Method.GET, uri"$baseUrl/mongodb/collections/${collectionName}/options")
      .contentType("application/json")
      .auth
      .basic(username, password)
      .auth
      .bearer(bearerToken)
      .header("X-AUTH-APIKEY", apiKey)
      .response(asJson[Json])

  def modifyCollectionOptions(username: String, password: String, bearerToken: String, apiKey: String)(collectionName: String, body: Json) =
    basicRequest
      .method(Method.PATCH, uri"$baseUrl/mongodb/collections/${collectionName}/options")
      .contentType("application/json")
      .auth
      .basic(username, password)
      .auth
      .bearer(bearerToken)
      .header("X-AUTH-APIKEY", apiKey)
      .body(body.noSpaces)
      .response(asJson[Json])
}
