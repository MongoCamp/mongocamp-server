package com.quadstingray.mongo.rest.routes

import com.quadstingray.mongo.rest.BuildInfo
import com.quadstingray.mongo.rest.config.SystemEnvironment
import com.quadstingray.mongo.rest.exception.ErrorDescription
import com.quadstingray.mongo.rest.model.Version
import com.sfxcode.nosql.mongo._
import com.sfxcode.nosql.mongo.database.{ CollectionStatus, DatabaseInfo, DatabaseProvider }
import io.circe.generic.auto._
import org.joda.time.DateTime
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

import scala.concurrent.Future

object InformationRoutes extends BaseRoute with SystemEnvironment {

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
      Right(Version(BuildInfo.name, BuildInfo.version, new DateTime(BuildInfo.builtAtMillis).toDate, systemStage))
    )
  }

  val databaseEndpoint = mongoConnectionEndpoint
    .in("databases")
    .out(jsonBody[List[String]])
    .summary("List of Databases")
    .description("List of all Databases")
    .tag("Information")
    .method(Method.GET)
    .name("databaseList")
    .serverLogic(connection => _ => databaseList(connection))

  def databaseList(database: DatabaseProvider): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[String]]] = {
    Future.successful(
      Right(
        {
          val result = database.databaseNames
          database.closeClient()
          result
        }
      )
    )
  }

  val collectionsEndpoint = mongoConnectionEndpoint
    .in("collections")
    .out(jsonBody[List[String]])
    .summary("List of Collections")
    .description("List of all Collections")
    .tag("Information")
    .method(Method.GET)
    .name("collectionList")
    .serverLogic(connection => _ => collectionList(connection))

  def collectionList(database: DatabaseProvider): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[String]]] = {
    Future.successful(
      Right(
        {
          val result = database.collectionNames()
          database.closeClient()
          result
        }
      )
    )
  }

  val collectionStatusEndpoint = collectionEndpoint
    .in("status")
    .in(query[Boolean]("includeDetails").description("Include all details for the Collection").default(false))
    .out(jsonBody[CollectionStatus])
    .summary("Status of Collection")
    .description("Collection Status")
    .tag("Information")
    .method(Method.GET)
    .name("collectionStatus")
    .serverLogic(connection => collection => collectionStatus(connection, collection))

  def collectionStatus(
      database: DatabaseProvider,
      parameter: (String, Boolean)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), CollectionStatus]] = {
    Future.successful(
      Right(
        {
          val dao    = database.dao(parameter._1)
          val result = dao.collectionStatus.result()
          database.closeClient()
          if (parameter._2) {
            result
          }
          else {
            result.copy(map = Map())
          }
        }
      )
    )
  }

  val databaseStatusEndpoint = mongoConnectionEndpoint
    .in("databases")
    .in("infos")
    .out(jsonBody[List[DatabaseInfo]])
    .summary("List of Database Infos")
    .description("List of all Databases Infos")
    .tag("Information")
    .method(Method.GET)
    .name("databaseInfos")
    .serverLogic(connection => _ => databaseInfos(connection))

  def databaseInfos(database: DatabaseProvider): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[DatabaseInfo]]] = {
    Future.successful(
      Right(
        {
          val result = database.databaseInfos
          database.closeClient()
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
