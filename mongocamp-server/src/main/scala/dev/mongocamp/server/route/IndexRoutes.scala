package dev.mongocamp.server.route

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.driver.mongodb.database.MongoIndex
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.model.auth.AuthorizedCollectionRequest
import dev.mongocamp.server.model.index.{ IndexCreateRequest, IndexCreateResponse, IndexDropResponse, IndexOptionsRequest }
import dev.mongocamp.server.plugin.RoutesPlugin
import io.circe.generic.auto._
import org.mongodb.scala.model.IndexOptions
import sttp.capabilities.WebSockets
import sttp.capabilities.pekko.PekkoStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future
import scala.concurrent.duration.Duration

object IndexRoutes extends CollectionBaseRoute with RoutesPlugin {

  val listIndexEndpoint = administrateCollectionEndpoint
    .in("index")
    .out(jsonBody[List[MongoIndex]])
    .summary("List Indices for Collection")
    .description("List all Indices for given Collection")
    .tag("Index")
    .method(Method.GET)
    .name("listIndices")
    .serverLogic(collectionRequest => _ => listIndicesInCollection(collectionRequest))

  def listIndicesInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[MongoIndex]]] = {
    Future.successful(
      Right(
        {
          val dao      = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val response = dao.indexList()
          response
        }
      )
    )
  }

  val indexByNameEndpoint = administrateCollectionEndpoint
    .in("index")
    .in(path[String]("indexName").description("The name of your Index"))
    .out(jsonBody[Option[MongoIndex]])
    .summary("Index for Collection")
    .description("Index by Name for given Collection")
    .tag("Index")
    .method(Method.GET)
    .name("index")
    .serverLogic(collectionRequest => parameter => indexByNameInCollection(collectionRequest, parameter))

  def indexByNameInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Option[MongoIndex]]] = {
    Future.successful(
      Right(
        {
          val dao      = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val response = dao.indexForName(parameter)
          response
        }
      )
    )
  }

  val createIndexEndpoint = administrateCollectionEndpoint
    .in("index")
    .in(jsonBody[IndexCreateRequest])
    .out(jsonBody[IndexCreateResponse])
    .summary("Create Index for Collection")
    .description("Create Index for given Collection")
    .tag("Index")
    .method(Method.PUT)
    .name("createIndex")
    .serverLogic(collectionRequest => parameter => createIndexByBsonInCollection(collectionRequest, parameter))

  def createIndexByBsonInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: IndexCreateRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), IndexCreateResponse]] = {
    Future.successful(
      Right(
        {
          val dao      = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val response = dao.createIndex(parameter.keys, requestToDBIndexOptions(parameter.indexOptionsRequest)).result()
          IndexCreateResponse(response)
        }
      )
    )
  }

  val createIndexForFieldEndpoint = administrateCollectionEndpoint
    .in("index")
    .in("field")
    .in(path[String]("fieldName").description("The field Name for your index"))
    .in(query[Boolean]("sortAscending").description("Sort your index ascending").default(true))
    .in(jsonBody[Option[IndexOptionsRequest]])
    .out(jsonBody[IndexCreateResponse])
    .summary("Create Index by Field for Collection")
    .description("Create Index by Field for given Collection")
    .tag("Index")
    .method(Method.PUT)
    .name("createIndexForField")
    .serverLogic(collectionRequest => parameter => createIndexForFieldInCollection(collectionRequest, parameter))

  def createIndexForFieldInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (String, Boolean, Option[IndexOptionsRequest])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), IndexCreateResponse]] = {
    Future.successful(
      Right(
        {
          val dao      = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val response = dao.createIndexForField(parameter._1, parameter._2, requestToDBIndexOptions(parameter._3)).result()
          IndexCreateResponse(response)
        }
      )
    )
  }

  val createUniqueIndexForFieldEndpoint = administrateCollectionEndpoint
    .in("index")
    .in("field")
    .in(path[String]("fieldName").description("The field Name for your index"))
    .in(query[Boolean]("sortAscending").description("Sort your index ascending").default(true))
    .in(query[Option[String]]("name").description("Name for your index").default(None))
    .in("unique")
    .out(jsonBody[IndexCreateResponse])
    .summary("Create Unique Index")
    .description("Create Unique Index by Field for given Collection")
    .tag("Index")
    .method(Method.PUT)
    .name("createUniqueIndex")
    .serverLogic(collectionRequest => parameter => createUniqueIndexForFieldInCollection(collectionRequest, parameter))

  def createUniqueIndexForFieldInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (String, Boolean, Option[String])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), IndexCreateResponse]] = {
    Future.successful(
      Right(
        {
          val dao      = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val response = dao.createUniqueIndexForField(parameter._1, parameter._2, parameter._3).result()
          IndexCreateResponse(response)
        }
      )
    )
  }

  val createExpiringIndexForFieldEndpoint = administrateCollectionEndpoint
    .in("index")
    .in("field")
    .in(path[String]("fieldName").description("The field Name for your index"))
    .in(
      path[String]("duration")
        .default("15d")
        .description("Expiring Duration in format 15d (https://www.scala-lang.org/api/2.13.7/scala/concurrent/duration/Duration.html)")
    )
    .in(query[Boolean]("sortAscending").description("Sort your index ascending").default(true))
    .in(query[Option[String]]("name").description("Name for your index").default(None))
    .in("expiring")
    .out(jsonBody[IndexCreateResponse])
    .summary("Create expiring Index by Field for Collection")
    .description("Create expiring Index by Field for given Collection")
    .tag("Index")
    .method(Method.PUT)
    .name("createExpiringIndex")
    .serverLogic(collectionRequest => parameter => createExpiringIndexForFieldInCollection(collectionRequest, parameter))

  def createExpiringIndexForFieldInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (String, String, Boolean, Option[String])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), IndexCreateResponse]] = {
    Future.successful(
      Right(
        {
          val dao      = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val response = dao.createExpiringIndexForField(parameter._1, Duration(parameter._2), parameter._3, parameter._4).result()
          IndexCreateResponse(response)
        }
      )
    )
  }

  val createTextIndexForFieldEndpoint = administrateCollectionEndpoint
    .in("index")
    .in("field")
    .in(path[String]("fieldName").description("The field Name for your index"))
    .in("text")
    .in(jsonBody[Option[IndexOptionsRequest]])
    .out(jsonBody[IndexCreateResponse])
    .summary("Create text index by field for collection")
    .description("Create text index by field for given collection")
    .tag("Index")
    .method(Method.PUT)
    .name("createTextIndex")
    .serverLogic(collectionRequest => parameter => createTextIndexForFieldInCollection(collectionRequest, parameter))

  def createTextIndexForFieldInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (String, Option[IndexOptionsRequest])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), IndexCreateResponse]] = {
    Future.successful(
      Right(
        {
          val dao      = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val response = dao.createTextIndexForField(parameter._1, requestToDBIndexOptions(parameter._2)).result()
          IndexCreateResponse(response)
        }
      )
    )
  }

  val deleteIndexEndpoint = administrateCollectionEndpoint
    .in("index")
    .in(path[String]("indexName").description("The name of your Index"))
    .out(jsonBody[IndexDropResponse])
    .summary("Delete Index")
    .description("Delete Index by Name for given Collection")
    .tag("Index")
    .method(Method.DELETE)
    .name("deleteIndex")
    .serverLogic(collectionRequest => parameter => dropIndexForFieldInCollection(collectionRequest, parameter))

  def dropIndexForFieldInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), IndexDropResponse]] = {
    Future.successful(
      Right(
        {
          val dao = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          dao.dropIndexForName(parameter).result()
          IndexDropResponse(true)
        }
      )
    )
  }

  def requestToDBIndexOptions(indexOptionsRequestOption: Option[IndexOptionsRequest]): IndexOptions = {
    var indexOptions = IndexOptions()
    indexOptionsRequestOption.foreach(indexOptionsRequest => {
      indexOptionsRequest.name.foreach(name => indexOptions = indexOptions.name(name))
      indexOptionsRequest.background.foreach(background => indexOptions = indexOptions.background(background))
      indexOptionsRequest.defaultLanguage.foreach(defaultLanguage => indexOptions = indexOptions.defaultLanguage(defaultLanguage))
      indexOptionsRequest.unique.foreach(unique => indexOptions = indexOptions.unique(unique))
      indexOptionsRequest.textVersion.foreach(textVersion => indexOptions = indexOptions.textVersion(textVersion))
      indexOptionsRequest.max.foreach(max => indexOptions = indexOptions.max(max))
      indexOptionsRequest.min.foreach(min => indexOptions = indexOptions.min(min))
      indexOptionsRequest.expireAfter.foreach(expireAfter => {
        val expire = scala.concurrent.duration.Duration(expireAfter)
        indexOptions = indexOptions.expireAfter(expire._1, expire._2)
      })
    })
    indexOptions
  }

  lazy val endpoints: List[ServerEndpoint[PekkoStreams with WebSockets, Future]] = List(
    listIndexEndpoint,
    indexByNameEndpoint,
    createIndexEndpoint,
    createIndexForFieldEndpoint,
    createUniqueIndexForFieldEndpoint,
    createExpiringIndexForFieldEndpoint,
    createTextIndexForFieldEndpoint,
    deleteIndexEndpoint
  )

}
