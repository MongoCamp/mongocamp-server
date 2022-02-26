package com.quadstingray.mongo.camp.routes

import com.quadstingray.mongo.camp.converter.MongoCampBsonConverter
import com.quadstingray.mongo.camp.database.MongoDatabase
import com.quadstingray.mongo.camp.database.paging.{ MongoPaginatedAggregation, MongoPaginatedFilter, PaginationInfo }
import com.quadstingray.mongo.camp.exception.ErrorDescription
import com.quadstingray.mongo.camp.model.auth.AuthorizedCollectionRequest
import com.quadstingray.mongo.camp.model.{ MongoAggregateRequest, MongoFindRequest }
import com.quadstingray.mongo.camp.routes.parameter.paging.{ Paging, PagingFunctions }
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

  val findAllEndpoint = readCollectionEndpoint
    .in(PagingFunctions.pagingParameter)
    .out(jsonBody[List[Map[String, Any]]])
    .out(PagingFunctions.pagingHeaderOutput)
    .summary("Data in Collection")
    .description("Search in your MongoDatabase Collection")
    .tag("Read")
    .method(Method.GET)
    .name("findAll")
    .serverLogic(collectionRequest => parameter => findAllInCollection(collectionRequest, parameter))

  def findAllInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (Paging)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (List[Map[String, Any]], PaginationInfo)]] = {
    findInCollection(authorizedCollectionRequest, (MongoFindRequest(Map(), Map(), Map()), parameter))
  }

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
    .in(PagingFunctions.pagingParameter)
    .out(jsonBody[List[Map[String, Any]]])
    .out(PagingFunctions.pagingHeaderOutput)
    .summary("Search in Collection")
    .description("Search in your MongoDatabase Collection")
    .tag("Read")
    .method(Method.POST)
    .name("find")
    .serverLogic(collectionRequest => parameter => findInCollection(collectionRequest, parameter))

  def findInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (MongoFindRequest, Paging)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (List[Map[String, Any]], PaginationInfo)]] = {
    Future.successful(
      Right(
        {
          val searchRequest = parameter._1
          val pagingInfo    = parameter._2
          val rowsPerPage   = pagingInfo.rowsPerPage.getOrElse(PagingFunctions.DefaultRowsPerPage)
          val page          = pagingInfo.page.getOrElse(1L)

          val mongoPaginatedFilter = MongoPaginatedFilter(
            MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection),
            searchRequest.filter,
            searchRequest.sort,
            searchRequest.projection
          )

          val findResult = mongoPaginatedFilter.paginate(rowsPerPage, page)
          (findResult.databaseObjects.map(MongoCampBsonConverter.documentToMap), findResult.paginationInfo)
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
    .in(PagingFunctions.pagingParameter)
    .out(jsonBody[List[Map[String, Any]]])
    .out(PagingFunctions.pagingHeaderOutput)
    .summary("Aggregate in Collection")
    .description("Aggregate in your MongoDatabase Collection")
    .tag("Read")
    .method(Method.POST)
    .name("aggregate")
    .serverLogic(collectionRequest => parameter => aggregateInCollection(collectionRequest, parameter))

  def aggregateInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (MongoAggregateRequest, Paging)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (List[Map[String, Any]], PaginationInfo)]] = {
    Future.successful(
      Right(
        {
          val mongoAggregateRequest = parameter._1
          val pagingInfo            = parameter._2
          val rowsPerPage           = pagingInfo.rowsPerPage.getOrElse(PagingFunctions.DefaultRowsPerPage)
          val page                  = pagingInfo.page.getOrElse(1L)

          val pipeline: Seq[Bson] = mongoAggregateRequest.pipeline.map(element => {
            val stage = if (element.stage.startsWith("$")) element.stage else "$" + element.stage
            mapToBson(Map(stage -> element.value))
          })

          val mongoPaginatedFilter = MongoPaginatedAggregation(
            MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection),
            mongoAggregateRequest.allowDiskUse,
            pipeline.toList
          )

          val aggregateResult = mongoPaginatedFilter.paginate(rowsPerPage, page)
          (aggregateResult.databaseObjects.map(MongoCampBsonConverter.documentToMap), aggregateResult.paginationInfo)

        }
      )
    )
  }

  val distinctEndpoint = readCollectionEndpoint
    .in("distinct")
    .in(path[String]("field").description("The field for your distinct Request."))
    .in(PagingFunctions.pagingParameter)
    .out(jsonBody[List[Any]])
    .out(PagingFunctions.pagingHeaderOutput)
    .summary("Distinct in Collection")
    .description("Distinct for Field in your MongoDatabase Collection")
    .tag("Read")
    .method(Method.POST)
    .name("distinct")
    .serverLogic(collectionRequest => parameter => distinctInCollection(collectionRequest, parameter))

  def distinctInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (String, Paging)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (List[Any], PaginationInfo)]] = {
    Future.successful(
      Right(
        {
          val fieldName   = "$" + parameter._1
          val pagingInfo  = parameter._2
          val rowsPerPage = pagingInfo.rowsPerPage.getOrElse(PagingFunctions.DefaultRowsPerPage)
          val page        = pagingInfo.page.getOrElse(1L)
          val mongoDao    = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val response = MongoPaginatedAggregation(
            dao = mongoDao,
            allowDiskUse = true,
            aggregationPipeline = List(Map("$group" -> Map("_id" -> fieldName, "field" -> Map("$first" -> fieldName))))
          ).paginate(rowsPerPage, page)

          (response.databaseObjects.map(document => BsonConverter.fromBson(document.get("field"))), response.paginationInfo)
        }
      )
    )
  }

  lazy val readEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = List(findAllEndpoint, findEndpoint, aggregateEndpoint, distinctEndpoint)

}
