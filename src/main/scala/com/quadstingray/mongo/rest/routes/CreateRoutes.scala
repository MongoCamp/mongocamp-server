package com.quadstingray.mongo.rest.routes

import com.quadstingray.mongo.rest.database.MongoDatabase
import com.quadstingray.mongo.rest.exception.ErrorDescription
import com.quadstingray.mongo.rest.model.InsertResponse
import com.quadstingray.mongo.rest.model.auth.AuthorizedCollectionRequest
import com.sfxcode.nosql.mongo._
import io.circe.generic.auto._
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import java.util.Date
import scala.concurrent.Future
import scala.jdk.CollectionConverters.CollectionHasAsScala

object CreateRoutes extends BaseRoute {

  val insertEndpoint = writeCollectionEndpoint
    .in("insert")
    .in(
      jsonBody[Map[String, Any]]
        .description("JSON Representation for your Document.")
        .example(Map("key1" -> "value", "key2" -> 0, "key2" -> true, "key3" -> Map("creationDate" -> new Date())))
    )
    .out(jsonBody[InsertResponse])
    .summary("Collection Insert")
    .description("Insert one Document in Collection")
    .tag("Create")
    .method(Method.PUT)
    .name("insert")
    .serverLogic(login => insert => insertInCollection(login, insert))

  def insertInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: Map[String, Any]
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), InsertResponse]] = {
    Future.successful(
      Right(
        {
          val dao            = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val result         = dao.insertOne(documentFromScalaMap(parameter)).result()
          val insertedResult = InsertResponse(result.wasAcknowledged(), List(result.getInsertedId.asObjectId().getValue.toHexString))
          insertedResult
        }
      )
    )
  }

  val insertManyEndpoint = writeCollectionEndpoint
    .in("insert")
    .in("many")
    .in(jsonBody[List[Map[String, Any]]])
    .out(jsonBody[InsertResponse])
    .summary("Collection Insert many")
    .description("Insert many documents in Collection")
    .tag("Create")
    .method(Method.PUT)
    .name("insertMany")
    .serverLogic(connection => insertList => insertManyInCollection(connection, insertList))

  def insertManyInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (List[Map[String, Any]])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), InsertResponse]] = {
    Future.successful(
      Right(
        {
          val dao                            = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val listOfDocuments                = parameter.map(documentFromScalaMap)
          val result                         = dao.insertMany(listOfDocuments).result()
          val listOfIds                      = result.getInsertedIds.values().asScala.map(_.asObjectId().getValue.toHexString).toList
          val insertedResult: InsertResponse = InsertResponse(result.wasAcknowledged(), listOfIds)
          insertedResult
        }
      )
    )
  }

  lazy val createEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = List(insertEndpoint, insertManyEndpoint)

}
