package com.quadstingray.mongo.rest.routes

import com.quadstingray.mongo.rest.converter.MongoRestBsonConverter
import com.quadstingray.mongo.rest.database.MongoDatabase
import com.quadstingray.mongo.rest.exception.ErrorDescription
import com.quadstingray.mongo.rest.model.auth.UserInformation
import com.quadstingray.mongo.rest.model.{ MongoAggregateRequest, MongoFindRequest }
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

  val findEndpoint = collectionEndpoint
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
    .serverLogic(connection => parameter => findInCollection(connection, parameter))

  def findInCollection(
      user: UserInformation,
      parameter: (String, MongoFindRequest)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Map[String, Any]]]] = {
    Future.successful(
      Right(
        {
          val dao       = MongoDatabase.databaseProvider.dao(parameter._1)
          val documents = dao.find(parameter._2.filter, parameter._2.sort, parameter._2.projection).resultList()
          documents.map(MongoRestBsonConverter.documentToMap)
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
  val aggregateEndpoint = collectionEndpoint
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
    .serverLogic(connection => parameter => aggregateInCollection(connection, parameter))

  def aggregateInCollection(
      user: UserInformation,
      parameter: (String, MongoAggregateRequest)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Map[String, Any]]]] = {
    Future.successful(
      Right(
        {
          val dao = MongoDatabase.databaseProvider.dao(parameter._1)
          val pipeline: Seq[Bson] = parameter._2.pipeline.map(element => {
            val stage = if (element.stage.startsWith("$")) element.stage else "$" + element.stage
            mapToBson(Map(stage -> element.value))
          })
          val documents = dao.findAggregated(pipeline, allowDiskUse = parameter._2.allowDiskUse).resultList()
          documents.map(MongoRestBsonConverter.documentToMap)
        }
      )
    )
  }

  val distinctEndpoint = collectionEndpoint
    .in("distinct")
    .in(path[String]("field").description("The field for your distinct Request."))
    .out(jsonBody[List[Any]])
    .summary("Distinct in Collection")
    .description("Distinct for Field in your MongoDatabase Collection")
    .tag("Read")
    .method(Method.POST)
    .name("distinct")
    .serverLogic(connection => parameter => distinctInCollection(connection, parameter))

  def distinctInCollection(
      user: UserInformation,
      parameter: (String, String)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Any]]] = {
    Future.successful(
      Right(
        {
          val dao       = MongoDatabase.databaseProvider.dao(parameter._1)
          val documents = dao.distinct(parameter._2).resultList()
          documents.map(BsonConverter.fromBson)
        }
      )
    )
  }

  lazy val readEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = List(findEndpoint, aggregateEndpoint, distinctEndpoint)

}
