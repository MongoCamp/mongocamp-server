package dev.mongocamp.server.test.client.api

import dev.mongocamp.server.test.client.core.ClientCirceSchema
import dev.mongocamp.server.test.client.core.JsonSupport._
import dev.mongocamp.server.test.TestServer
import io.circe.Json
import sttp.client3._
import sttp.model.Method

object CollectionRenameApi {

  def apply(baseUrl: String = TestServer.serverBaseUrl) = new CollectionRenameApi(baseUrl)
}

/** Rename and copy a Collection in the database. A class of its own, the generated CollectionApi is not edited by hand (see DocumentCountApi). */
class CollectionRenameApi(baseUrl: String) extends ClientCirceSchema {

  def renameCollection(username: String, password: String, bearerToken: String, apiKey: String)(collectionName: String, body: Json) =
    basicRequest
      .method(Method.POST, uri"$baseUrl/mongodb/collections/${collectionName}/rename")
      .contentType("application/json")
      .auth
      .basic(username, password)
      .auth
      .bearer(bearerToken)
      .header("X-AUTH-APIKEY", apiKey)
      .body(body.noSpaces)
      .response(asJson[Json])

  def copyCollection(username: String, password: String, bearerToken: String, apiKey: String)(collectionName: String, body: Json) =
    basicRequest
      .method(Method.POST, uri"$baseUrl/mongodb/collections/${collectionName}/copy")
      .contentType("application/json")
      .auth
      .basic(username, password)
      .auth
      .bearer(bearerToken)
      .header("X-AUTH-APIKEY", apiKey)
      .body(body.noSpaces)
      .response(asJson[Json])
}
