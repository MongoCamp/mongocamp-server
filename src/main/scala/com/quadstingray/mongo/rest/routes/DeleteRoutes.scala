package com.quadstingray.mongo.rest.routes

import com.quadstingray.mongo.rest.database.MongoDatabase
import com.quadstingray.mongo.rest.exception.ErrorDescription
import com.quadstingray.mongo.rest.model.DeleteResponse
import com.quadstingray.mongo.rest.model.auth.AuthorizedCollectionRequest
import com.sfxcode.nosql.mongo._
import io.circe.generic.auto._
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object DeleteRoutes extends BaseRoute {

  val deleteEndpoint = writeCollectionEndpoint
    .in("delete")
    .in(jsonBody[Map[String, Any]])
    .out(jsonBody[DeleteResponse])
    .summary("Delete one in Collection")
    .description("Delete one Document in Collection")
    .tag("Delete")
    .method(Method.DELETE)
    .name("delete")
    .serverLogic(collectionRequest => search => deleteInCollection(collectionRequest, search))

  def deleteInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: Map[String, Any]
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), DeleteResponse]] = {
    Future.successful(
      Right(
        {
          val dao            = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val result         = dao.deleteOne(parameter).result()
          val deleteResponse = DeleteResponse(result.wasAcknowledged(), result.getDeletedCount)
          deleteResponse
        }
      )
    )
  }

  val deleteManyEndpoint = writeCollectionEndpoint
    .in("delete")
    .in("many")
    .in(jsonBody[Map[String, Any]])
    .out(jsonBody[DeleteResponse])
    .summary("Delete Many in Collection")
    .description("Delete many Document in Collection")
    .tag("Delete")
    .method(Method.DELETE)
    .name("deleteMany")
    .serverLogic(collectionRequest => search => deleteManyInCollection(collectionRequest, search))

  def deleteManyInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: Map[String, Any]
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), DeleteResponse]] = {
    Future.successful(
      Right(
        {
          val dao            = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val result         = dao.deleteMany(parameter).result()
          val deleteResponse = DeleteResponse(result.wasAcknowledged(), result.getDeletedCount)
          deleteResponse
        }
      )
    )
  }

  val deleteAllEndpoint = writeCollectionEndpoint
    .in("delete")
    .in("all")
    .out(jsonBody[DeleteResponse])
    .summary("Delete all in Collection")
    .description("Delete all Document in Collection")
    .tag("Delete")
    .method(Method.DELETE)
    .name("deleteAll")
    .serverLogic(collectionRequest => _ => deleteManyInCollection(collectionRequest))

  def deleteManyInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), DeleteResponse]] = {
    Future.successful(
      Right(
        {
          val dao            = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val result         = dao.deleteAll().result()
          val deleteResponse = DeleteResponse(result.wasAcknowledged(), result.getDeletedCount)
          deleteResponse
        }
      )
    )
  }

  lazy val deleteEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = List(deleteEndpoint, deleteManyEndpoint, deleteAllEndpoint)

}
