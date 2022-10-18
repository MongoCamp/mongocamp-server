package dev.mongocamp.server.route

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.driver.mongodb.database.DatabaseInfo
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.database.MongoDatabase.databaseProvider.DefaultDatabaseName
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.database.DropDatabaseEvent
import dev.mongocamp.server.exception.{ErrorDescription, MongoCampException}
import dev.mongocamp.server.model.BucketInformation.BucketCollectionSuffix
import dev.mongocamp.server.model.JsonValue
import dev.mongocamp.server.model.auth.{AuthorizedCollectionRequest, UserInformation}
import dev.mongocamp.server.plugin.RoutesPlugin
import io.circe.generic.auto._
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{Method, StatusCode}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object DatabaseRoutes extends RoutesPlugin {

  private val databaseBaseEndpoint = adminEndpoint.tag("Database").in(mongoDbPath).in("databases")

  val databaseEndpoint = databaseBaseEndpoint
    .out(jsonBody[List[String]])
    .summary("List of Databases")
    .description("List of all Databases")
    .method(Method.GET)
    .name("ListDatabases")
    .serverLogic(_ => _ => databaseList())

  def databaseList(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[String]]] = {
    Future.successful(Right({
      val result = MongoDatabase.databaseProvider.databaseNames
      result
    }))
  }

  val databaseStatusEndpoint = databaseBaseEndpoint
    .in("infos")
    .out(jsonBody[List[DatabaseInfo]])
    .summary("List of Database Infos")
    .description("List of all Databases Infos")
    .method(Method.GET)
    .name("databaseInfos")
    .serverLogic(_ => _ => databaseInfos())

  def databaseInfos(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[DatabaseInfo]]] = {
    Future.successful(Right({
      val result = MongoDatabase.databaseProvider.databaseInfos
      result
    }))
  }

  val databaseInfoEndpoint = databaseBaseEndpoint
    .in(path[String]("databaseName").description("Name of your Database"))
    .out(jsonBody[DatabaseInfo])
    .summary("Database Infos of selected Database")
    .description("All Information about given Database")
    .method(Method.GET)
    .name("getDatabaseInfo")
    .serverLogic(_ => databaseName => getDatabaseInfo(databaseName))

  def getDatabaseInfo(databaseName: String): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), DatabaseInfo]] = {
    Future.successful(Right({
      val result = MongoDatabase.databaseProvider.databaseInfos.find(_.name.equalsIgnoreCase(databaseName))
      result.getOrElse(throw MongoCampException("database not found", StatusCode.NotFound))
    }))
  }

  val deleteDatabaseEndpoint = databaseBaseEndpoint
    .in(path[String]("databaseName").description("Name of your Database"))
    .out(jsonBody[JsonValue[Boolean]])
    .summary("Delete Database")
    .description("Delete given Database")
    .method(Method.DELETE)
    .name("deleteDatabase")
    .serverLogic(userInformation => databaseName => deleteDatabaseInfo(userInformation, databaseName))

  def deleteDatabaseInfo(
      userInformation: UserInformation,
      databaseName: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful(Right({
      MongoDatabase.databaseProvider.dropDatabase(databaseName).result()
      EventSystem.eventStream.publish(DropDatabaseEvent(userInformation, databaseName))
      JsonValue(true)
    }))
  }

  val collectionsEndpoint = securedEndpoint
    .tag("Database")
    .in(mongoDbPath)
    .in("databases")
    .in(path[String]("databaseName").description("Name of your Database"))
    .in("collections")
    .out(jsonBody[List[String]])
    .summary("List of Collections")
    .description("List of all Collections of the given database")
    .tag("Collection")
    .method(Method.GET)
    .name("listCollectionsByDatabase")
    .serverLogic(user => parameter => collectionList(user, parameter))

  def collectionList(userInformation: UserInformation, database: String): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[String]]] = {
    Future.successful(Right({
      val result           = MongoDatabase.databaseProvider.collectionNames(database)
      val collectionGrants = userInformation.getCollectionGrants
      result.filter(coll => {
        val collection      = if (database.equalsIgnoreCase(DefaultDatabaseName)) coll else s"$database:$coll"
        val readCollections = collectionGrants.filter(_.read).map(_.name)
        val allBucketMetaFilter =
          readCollections.contains(s"${AuthorizedCollectionRequest.all}$BucketCollectionSuffix") && collection.endsWith(BucketCollectionSuffix)
        userInformation.isAdmin || readCollections.contains(AuthorizedCollectionRequest.all) || readCollections.contains(collection) || allBucketMetaFilter
      })
    }))
  }

  override def endpoints: List[ServerEndpoint[AkkaStreams with capabilities.WebSockets, Future]] =
    List(databaseEndpoint, databaseStatusEndpoint, databaseInfoEndpoint, deleteDatabaseEndpoint, collectionsEndpoint)

}
