package com.quadstingray.mongo.camp.routes

import com.quadstingray.mongo.camp.database.MongoDatabase
import com.quadstingray.mongo.camp.exception.ErrorDescription
import com.quadstingray.mongo.camp.model.auth.{ AuthorizedCollectionRequest, UserInformation }
import com.quadstingray.mongo.camp.model.{ DeleteResponse, JsonResult }
import com.sfxcode.nosql.mongo._
import com.sfxcode.nosql.mongo.database.CollectionStatus
import io.circe.generic.auto._
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object CollectionRoutes extends RoutesPlugin {

  val collectionsEndpoint = securedEndpoint
    .in(mongoDbPath)
    .in("collections")
    .out(jsonBody[List[String]])
    .summary("List of Collections")
    .description("List of all Collections")
    .tag("Collection")
    .method(Method.GET)
    .name("collectionList")
    .serverLogic(user => _ => collectionList(user))

  def collectionList(userInformation: UserInformation): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[String]]] = {
    Future.successful(Right({
      val result           = MongoDatabase.databaseProvider.collectionNames()
      val collectionGrants = userInformation.toResultUser.collectionGrant
      result.filter(collection => {
        val readCollections = collectionGrants.filter(_.read).map(_.collection)
        userInformation.isAdmin || readCollections.contains(AuthorizedCollectionRequest.allCollections) || readCollections.contains(collection)
      })
    }))
  }

  val getCollectionStatusEndpoint = readCollectionEndpoint
    .in(query[Boolean]("includeDetails").description("Include all details for the Collection").default(false))
    .out(jsonBody[CollectionStatus])
    .summary("Collection Information")
    .description("All Informations about a single Collection")
    .tag("Collection")
    .method(Method.GET)
    .name("collectionInformation")
    .serverLogic(collectionRequest => filter => collectionStatus(collectionRequest, filter))

  def collectionStatus(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: Boolean
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), CollectionStatus]] = {
    Future.successful(Right({
      val dao    = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
      val result = dao.collectionStatus.result()
      if (parameter) {
        result
      }
      else {
        result.copy(map = Map())
      }
    }))
  }

  val deleteCollectionStatusEndpoint = administrateCollectionEndpoint
    .out(jsonBody[JsonResult[Boolean]])
    .summary("Delete Collection")
    .description("Delete Collection")
    .tag("Collection")
    .method(Method.DELETE)
    .name("deleteCollection")
    .serverLogic(collectionRequest => _ => deleteCollection(collectionRequest))

  def deleteCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonResult[Boolean]]] = {
    Future.successful(Right({
      val dao = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
      dao.drop().result()
      JsonResult(true)
    }))
  }

  val deleteAllEndpoint = writeCollectionEndpoint
    .in("clear")
    .out(jsonBody[DeleteResponse])
    .summary("Clear Collection")
    .description("Delete all Document in Collection")
    .tag("Collection")
    .method(Method.DELETE)
    .name("clearCollection")
    .serverLogic(collectionRequest => _ => deleteAllInCollection(collectionRequest))

  def deleteAllInCollection(
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

  override def endpoints: List[ServerEndpoint[AkkaStreams with capabilities.WebSockets, Future]] =
    List(collectionsEndpoint, getCollectionStatusEndpoint, deleteCollectionStatusEndpoint, deleteAllEndpoint)

}
