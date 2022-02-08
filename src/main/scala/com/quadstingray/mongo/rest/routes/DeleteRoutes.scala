package com.quadstingray.mongo.rest.routes

import com.quadstingray.mongo.rest.database.MongoDatabase
import com.quadstingray.mongo.rest.exception.ErrorDescription
import com.quadstingray.mongo.rest.model.{ DeleteResponse, UserInformation }
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

  val deleteEndpoint = collectionEndpoint
    .in("delete")
    .in(jsonBody[Map[String, Any]])
    .out(jsonBody[DeleteResponse])
    .summary("Delete one in Collection")
    .description("Delete one Document in Collection")
    .tag("Delete")
    .method(Method.DELETE)
    .name("delete")
    .serverLogic(connection => search => deleteInCollection(connection, search))

  def deleteInCollection(
      user: UserInformation,
      parameter: (String, Map[String, Any])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), DeleteResponse]] = {
    Future.successful(
      Right(
        {
          val dao            = MongoDatabase.databaseProvider.dao(parameter._1)
          val result         = dao.deleteOne(parameter._2).result()
          val deleteResponse = DeleteResponse(result.wasAcknowledged(), result.getDeletedCount)
          deleteResponse
        }
      )
    )
  }

  val deleteManyEndpoint = collectionEndpoint
    .in("delete")
    .in("many")
    .in(jsonBody[Map[String, Any]])
    .out(jsonBody[DeleteResponse])
    .summary("Delete Many in Collection")
    .description("Delete many Document in Collection")
    .tag("Delete")
    .method(Method.DELETE)
    .name("deleteMany")
    .serverLogic(connection => search => deleteManyInCollection(connection, search))

  def deleteManyInCollection(
      user: UserInformation,
      parameter: (String, Map[String, Any])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), DeleteResponse]] = {
    Future.successful(
      Right(
        {
          val dao            = MongoDatabase.databaseProvider.dao(parameter._1)
          val result         = dao.deleteMany(parameter._2).result()
          val deleteResponse = DeleteResponse(result.wasAcknowledged(), result.getDeletedCount)
          deleteResponse
        }
      )
    )
  }

  val deleteAllEndpoint = collectionEndpoint
    .in("delete")
    .in("all")
    .out(jsonBody[DeleteResponse])
    .summary("Delete all in Collection")
    .description("Delete all Document in Collection")
    .tag("Delete")
    .method(Method.DELETE)
    .name("deleteAll")
    .serverLogic(connection => search => deleteManyInCollection(connection, search))

  def deleteManyInCollection(
      user: UserInformation,
      parameter: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), DeleteResponse]] = {
    Future.successful(
      Right(
        {
          val dao            = MongoDatabase.databaseProvider.dao(parameter)
          val result         = dao.deleteAll().result()
          val deleteResponse = DeleteResponse(result.wasAcknowledged(), result.getDeletedCount)
          deleteResponse
        }
      )
    )
  }

  lazy val deleteEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = List(deleteEndpoint, deleteManyEndpoint, deleteAllEndpoint)

}
