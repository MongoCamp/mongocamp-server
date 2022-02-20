package com.quadstingray.mongo.camp.routes

import com.quadstingray.mongo.camp.converter.MongoCampBsonConverter
import com.quadstingray.mongo.camp.database.MongoDatabase
import com.quadstingray.mongo.camp.exception.ErrorDescription
import com.quadstingray.mongo.camp.model.auth.AuthorizedCollectionRequest
import com.quadstingray.mongo.camp.model.{ MongoAggregateRequest, MongoFindRequest }
import com.sfxcode.nosql.mongo._
import com.sfxcode.nosql.mongo.bson.BsonConverter
import io.circe.generic.auto._
import org.bson.conversions.Bson
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object ReadRoutes extends BaseRoute {

  val findEndpoint = readCollectionEndpoint
    .in("find")
    .in(
      jsonBody[MongoFindRequest].example(
        MongoFindRequest(
          Map("additionalProp1" -> "string", "additionalProp2" -> 123),
          Map("additionalProp2" -> -1),
          Map("additionalProp1" -> 1, "additionalProp2"        -> 1)
        )
      )
    )
    .out(jsonBody[List[Map[String, Any]]])
    .summary("Search in Collection")
    .description("Search in your MongoDatabase Collection")
    .tag("Read")
    .method(Method.POST)
    .name("find")
    .serverLogic(collectionRequest => parameter => findInCollection(collectionRequest, parameter))

  def findInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: MongoFindRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Map[String, Any]]]] = {
    Future.successful(
      Right(
        {
          val dao       = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val documents = dao.find(parameter.filter, parameter.sort, parameter.projection).resultList()
          documents.map(MongoCampBsonConverter.documentToMap)
        }
      )
    )
  }

  // todo: OpenAPI Generation could not build Sample
  /*
  {
  "pipeline": [
    {
      "stage": "match",
      "value": { "additionalProp2": "string" }
    },
    {
      "stage": "count",
      "value": "additionalProp1"
    }
  ],
  "allowDiskUse": true
}
   */
  val aggregateEndpoint = readCollectionEndpoint
    .in("aggregate")
    .in(
      jsonBody[MongoAggregateRequest]
//        .example(
//          MongoAggregateRequest(List(PipelineStage("match", Map("additionalProp2" -> "string")), PipelineStage("count", "additionalProp1")))
//        )
    )
    .out(jsonBody[List[Map[String, Any]]])
    .summary("Aggregate in Collection")
    .description("Aggregate in your MongoDatabase Collection")
    .tag("Read")
    .method(Method.POST)
    .name("aggregate")
    .serverLogic(collectionRequest => parameter => aggregateInCollection(collectionRequest, parameter))

  def aggregateInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: MongoAggregateRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Map[String, Any]]]] = {
    Future.successful(
      Right(
        {
          val dao = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val pipeline: Seq[Bson] = parameter.pipeline.map(element => {
            val stage = if (element.stage.startsWith("$")) element.stage else "$" + element.stage
            mapToBson(Map(stage -> element.value))
          })
          val documents = dao.findAggregated(pipeline, allowDiskUse = parameter.allowDiskUse).resultList()
          documents.map(MongoCampBsonConverter.documentToMap)
        }
      )
    )
  }

  val distinctEndpoint = readCollectionEndpoint
    .in("distinct")
    .in(path[String]("field").description("The field for your distinct Request."))
    .out(jsonBody[List[Any]])
    .summary("Distinct in Collection")
    .description("Distinct for Field in your MongoDatabase Collection")
    .tag("Read")
    .method(Method.POST)
    .name("distinct")
    .serverLogic(collectionRequest => parameter => distinctInCollection(collectionRequest, parameter))

  def distinctInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Any]]] = {
    Future.successful(
      Right(
        {
          val dao       = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val documents = dao.distinct(parameter).resultList()
          documents.map(BsonConverter.fromBson)
        }
      )
    )
  }

  lazy val readEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = List(findEndpoint, aggregateEndpoint, distinctEndpoint)

}
