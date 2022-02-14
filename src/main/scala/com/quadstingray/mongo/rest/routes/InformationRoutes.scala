package com.quadstingray.mongo.rest.routes

import com.quadstingray.mongo.rest.BuildInfo
import com.quadstingray.mongo.rest.database.MongoDatabase
import com.quadstingray.mongo.rest.exception.ErrorDescription
import com.quadstingray.mongo.rest.model.Version
import com.quadstingray.mongo.rest.model.auth.{ AuthorizedCollectionRequest, UserInformation }
import com.sfxcode.nosql.mongo._
import com.sfxcode.nosql.mongo.database.{ CollectionStatus, DatabaseInfo }
import io.circe.generic.auto._
import org.joda.time.DateTime
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

import scala.concurrent.Future

object InformationRoutes extends BaseRoute {

  val version = baseEndpoint
    .in("version")
    .out(jsonBody[Version])
    .summary("Version Information")
    .description("Version Info of the MongoRest API")
    .tag("Information")
    .method(Method.GET)
    .name("version")
    .serverLogic(_ => createVersion())

  def createVersion(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Version]] = {
    Future.successful(
      Right(Version(BuildInfo.name, BuildInfo.version, new DateTime(BuildInfo.builtAtMillis).toDate))
    )
  }

  val databaseEndpoint = adminEndpoint
    .in("databases")
    .out(jsonBody[List[String]])
    .summary("List of Databases")
    .description("List of all Databases")
    .tag("Information")
    .method(Method.GET)
    .name("databaseList")
    .serverLogic(_ => _ => databaseList())

  def databaseList(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[String]]] = {
    Future.successful(
      Right(
        {
          val result = MongoDatabase.databaseProvider.databaseNames
          result
        }
      )
    )
  }

  val collectionsEndpoint = securedEndpoint
    .in("collections")
    .out(jsonBody[List[String]])
    .summary("List of Collections")
    .description("List of all Collections")
    .tag("Information")
    .method(Method.GET)
    .name("collectionList")
    .serverLogic(user => _ => collectionList(user))

  def collectionList(userInformation: UserInformation): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[String]]] = {
    Future.successful(
      Right(
        {
          val result           = MongoDatabase.databaseProvider.collectionNames()
          val collectionGrants = userInformation.toResultUser.collectionGrant
          result.filter(collection => {
            val readCollections = collectionGrants.filter(_.read).map(_.collection)
            userInformation.isAdmin || readCollections.contains(AuthorizedCollectionRequest.allCollections) || readCollections.contains(collection)
          })
        }
      )
    )
  }

  val collectionStatusEndpoint = readCollectionEndpoint
    .in("status")
    .in(query[Boolean]("includeDetails").description("Include all details for the Collection").default(false))
    .out(jsonBody[CollectionStatus])
    .summary("Status of Collection")
    .description("Collection Status")
    .tag("Information")
    .method(Method.GET)
    .name("collectionStatus")
    .serverLogic(collectionRequest => filter => collectionStatus(collectionRequest, filter))

  def collectionStatus(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: Boolean
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), CollectionStatus]] = {
    Future.successful(
      Right(
        {
          val dao    = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val result = dao.collectionStatus.result()
          if (parameter) {
            result
          }
          else {
            result.copy(map = Map())
          }
        }
      )
    )
  }

  val databaseStatusEndpoint = adminEndpoint
    .in("databases")
    .in("infos")
    .out(jsonBody[List[DatabaseInfo]])
    .summary("List of Database Infos")
    .description("List of all Databases Infos")
    .tag("Information")
    .method(Method.GET)
    .name("databaseInfos")
    .serverLogic(_ => _ => databaseInfos())

  def databaseInfos(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[DatabaseInfo]]] = {
    Future.successful(
      Right(
        {
          val result = MongoDatabase.databaseProvider.databaseInfos
          result
        }
      )
    )
  }

  val informationRoutes = List(
    version,
    databaseEndpoint,
    databaseStatusEndpoint,
    collectionsEndpoint,
    collectionStatusEndpoint
  )
}
