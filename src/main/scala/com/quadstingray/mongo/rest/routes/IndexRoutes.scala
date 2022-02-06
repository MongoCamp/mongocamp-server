package com.quadstingray.mongo.rest.routes

import com.quadstingray.mongo.rest.config.SystemEnvironment
import com.quadstingray.mongo.rest.exception.ErrorDescription
import com.quadstingray.mongo.rest.model.index.{ IndexCreateRequest, IndexCreateResponse, IndexDropResponse, IndexOptionsRequest }
import com.sfxcode.nosql.mongo._
import com.sfxcode.nosql.mongo.database.{ DatabaseProvider, MongoIndex }
import io.circe.generic.auto._
import org.mongodb.scala.model.IndexOptions
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future
import scala.concurrent.duration.Duration

object IndexRoutes extends BaseRoute with SystemEnvironment {

  val listIndexEndpoint = collectionEndpoint
    .in("index")
    .out(jsonBody[List[MongoIndex]])
    .summary("List Indices for Collection")
    .description("List all Indices for Collection")
    .tag("Index")
    .method(Method.GET)
    .name("indexList")
    .serverLogic(connection => parameter => listIndicesInCollection(connection, parameter))

  def listIndicesInCollection(
      database: DatabaseProvider,
      parameter: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[MongoIndex]]] = {
    Future.successful(
      Right(
        {
          val dao      = database.dao(parameter)
          val response = dao.indexList()
          database.closeClient()
          response
        }
      )
    )
  }

  val indexByNameEndpoint = collectionEndpoint
    .in("index")
    .in(path[String]("indexName").description("The name of your Index"))
    .out(jsonBody[Option[MongoIndex]])
    .summary("Index for Collection")
    .description("Index by Name for Collection")
    .tag("Index")
    .method(Method.GET)
    .name("index")
    .serverLogic(connection => parameter => indexByNameInCollection(connection, parameter))

  def indexByNameInCollection(
      database: DatabaseProvider,
      parameter: (String, String)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Option[MongoIndex]]] = {
    Future.successful(
      Right(
        {
          val dao      = database.dao(parameter._1)
          val response = dao.indexForName(parameter._2)
          database.closeClient()
          response
        }
      )
    )
  }

  val createIndexEndpoint = collectionEndpoint
    .in("index")
    .in(jsonBody[IndexCreateRequest])
    .out(jsonBody[IndexCreateResponse])
    .summary("Create Index for Collection")
    .description("Create Index for Collection")
    .tag("Index")
    .method(Method.PUT)
    .name("createIndex")
    .serverLogic(connection => parameter => createIndexByBsonInCollection(connection, parameter))

  def createIndexByBsonInCollection(
      database: DatabaseProvider,
      parameter: (String, IndexCreateRequest)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), IndexCreateResponse]] = {
    Future.successful(
      Right(
        {
          val dao      = database.dao(parameter._1)
          val response = dao.createIndex(parameter._2.keys, requestToDBIndexOptions(parameter._2.indexOptionsRequest)).result()
          database.closeClient()
          IndexCreateResponse(response)
        }
      )
    )
  }

  val createIndexForFieldEndpoint = collectionEndpoint
    .in("index")
    .in("field")
    .in(path[String]("fieldName").description("The field Name for your index"))
    .in(query[Boolean]("sortAscending").description("Sort your index ascending").default(true))
    .in(jsonBody[IndexOptionsRequest])
    .out(jsonBody[IndexCreateResponse])
    .summary("Create Index by Field for Collection")
    .description("Create Index by Field for Collection")
    .tag("Index")
    .method(Method.PUT)
    .name("createIndexForField")
    .serverLogic(connection => parameter => createIndexForFieldInCollection(connection, parameter))

  def createIndexForFieldInCollection(
      database: DatabaseProvider,
      parameter: (String, String, Boolean, IndexOptionsRequest)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), IndexCreateResponse]] = {
    Future.successful(
      Right(
        {
          val dao      = database.dao(parameter._1)
          val response = dao.createIndexForField(parameter._2, parameter._3, requestToDBIndexOptions(parameter._4)).result()
          database.closeClient()
          IndexCreateResponse(response)
        }
      )
    )
  }

  val createUniqueIndexForFieldEndpoint = collectionEndpoint
    .in("index")
    .in("field")
    .in(path[String]("fieldName").description("The field Name for your index"))
    .in(query[Boolean]("sortAscending").description("Sort your index ascending").default(true))
    .in(query[Option[String]]("name").description("Name for your index").default(None))
    .in("unique")
    .out(jsonBody[IndexCreateResponse])
    .summary("Create Index by Field for Collection")
    .description("Create Index by Field for Collection")
    .tag("Index")
    .method(Method.PUT)
    .name("createUniqueIndexForField")
    .serverLogic(connection => parameter => createUniqueIndexForFieldInCollection(connection, parameter))

  def createUniqueIndexForFieldInCollection(
      database: DatabaseProvider,
      parameter: (String, String, Boolean, Option[String])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), IndexCreateResponse]] = {
    Future.successful(
      Right(
        {
          val dao      = database.dao(parameter._1)
          val response = dao.createUniqueIndexForField(parameter._2, parameter._3, parameter._4).result()
          database.closeClient()
          IndexCreateResponse(response)
        }
      )
    )
  }

  val createExpiringIndexForFieldEndpoint = collectionEndpoint
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
    .summary("Create Index by Field for Collection")
    .description("Create Index by Field for Collection")
    .tag("Index")
    .method(Method.PUT)
    .name("createExpiringIndexForField")
    .serverLogic(connection => parameter => createExpiringIndexForFieldInCollection(connection, parameter))

  def createExpiringIndexForFieldInCollection(
      database: DatabaseProvider,
      parameter: (String, String, String, Boolean, Option[String])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), IndexCreateResponse]] = {
    Future.successful(
      Right(
        {
          val dao      = database.dao(parameter._1)
          val response = dao.createExpiringIndexForField(parameter._2, Duration(parameter._3), parameter._4, parameter._5).result()
          database.closeClient()
          IndexCreateResponse(response)
        }
      )
    )
  }

  val createTextIndexForFieldEndpoint = collectionEndpoint
    .in("index")
    .in("field")
    .in(path[String]("fieldName").description("The field Name for your index"))
    .in("text")
    .in(jsonBody[IndexOptionsRequest])
    .out(jsonBody[IndexCreateResponse])
    .summary("Create Index by Field for Collection")
    .description("Create Index by Field for Collection")
    .tag("Index")
    .method(Method.PUT)
    .name("createTextIndexForField")
    .serverLogic(connection => parameter => createTextIndexForFieldInCollection(connection, parameter))

  def createTextIndexForFieldInCollection(
      database: DatabaseProvider,
      parameter: (String, String, IndexOptionsRequest)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), IndexCreateResponse]] = {
    Future.successful(
      Right(
        {
          val dao      = database.dao(parameter._1)
          val response = dao.createTextIndexForField(parameter._2, requestToDBIndexOptions(parameter._3)).result()
          database.closeClient()
          IndexCreateResponse(response)
        }
      )
    )
  }

  val deleteIndexEndpoint = collectionEndpoint
    .in("index")
    .in(path[String]("indexName").description("The name of your Index"))
    .out(jsonBody[IndexDropResponse])
    .summary("Delete Index")
    .description("Delete Index by Name for Collection")
    .tag("Index")
    .method(Method.DELETE)
    .name("deleteIndex")
    .serverLogic(connection => parameter => dropIndexForFieldInCollection(connection, parameter))

  def dropIndexForFieldInCollection(
      database: DatabaseProvider,
      parameter: (String, String)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), IndexDropResponse]] = {
    Future.successful(
      Right(
        {
          val dao = database.dao(parameter._1)
          dao.dropIndexForName(parameter._2).result()
          database.closeClient()
          IndexDropResponse(true)
        }
      )
    )
  }

  def requestToDBIndexOptions(indexOptionsRequest: IndexOptionsRequest): IndexOptions = {
    var indexOptions = IndexOptions()
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
    indexOptions
  }

  lazy val indexEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = List(
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
