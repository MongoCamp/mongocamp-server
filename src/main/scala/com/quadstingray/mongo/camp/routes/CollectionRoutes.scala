package com.quadstingray.mongo.camp.routes

import com.quadstingray.mongo.camp.converter.MongoCampBsonConverter
import com.quadstingray.mongo.camp.database.MongoDatabase
import com.quadstingray.mongo.camp.database.paging.{ MongoPaginatedAggregation, PaginationInfo }
import com.quadstingray.mongo.camp.exception.ErrorDescription
import com.quadstingray.mongo.camp.model.BucketInformation.BucketCollectionSuffix
import com.quadstingray.mongo.camp.model.auth.{ AuthorizedCollectionRequest, UserInformation }
import com.quadstingray.mongo.camp.model.{ JsonResult, MongoAggregateRequest }
import com.quadstingray.mongo.camp.routes.parameter.paging.{ Paging, PagingFunctions }
import com.sfxcode.nosql.mongo._
import com.sfxcode.nosql.mongo.bson.BsonConverter
import com.sfxcode.nosql.mongo.database.CollectionStatus
import io.circe.generic.auto._
import org.bson.conversions.Bson
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object CollectionRoutes extends CollectionBaseRoute with RoutesPlugin {

  val collectionsEndpoint = securedEndpoint
    .in(mongoDbPath)
    .in("collections")
    .out(jsonBody[List[String]])
    .summary("List of Collections")
    .description("List of all Collections of the default database")
    .tag("Collection")
    .method(Method.GET)
    .name("listCollections")
    .serverLogic(user => _ => collectionList(user))

  def collectionList(userInformation: UserInformation): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[String]]] = {
    Future.successful(Right({
      val result           = MongoDatabase.databaseProvider.collectionNames()
      val collectionGrants = userInformation.getCollectionGrants
      result.filter(collection => {
        val readCollections = collectionGrants.filter(_.read).map(_.name)
        val allBucketMetaFilter =
          readCollections.contains(s"${AuthorizedCollectionRequest.all}$BucketCollectionSuffix") && collection.endsWith(BucketCollectionSuffix)
        userInformation.isAdmin || readCollections.contains(AuthorizedCollectionRequest.all) || readCollections.contains(collection) || allBucketMetaFilter
      })
    }))
  }

  val getCollectionStatusEndpoint = readCollectionEndpoint
    .in(query[Boolean]("includeDetails").description("Include all details for the Collection").default(false))
    .out(jsonBody[CollectionStatus])
    .summary("Collection Information")
    .description("All Information about a single Collection")
    .tag("Collection")
    .method(Method.GET)
    .name("getCollectionInformation")
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

  val getCollectionFieldsEndpoint = readCollectionEndpoint
    .in("fields")
    .in(query[Option[Int]]("sample size").example(Some(1000)).description("Use sample size greater 0 (e.g. 1000) for better performance on big collections"))
    .out(jsonBody[List[String]])
    .summary("Collection Fields")
    .description("List the Fields in a given collection")
    .tag("Collection")
    .method(Method.GET)
    .name("getCollectionFields")
    .serverLogic(collectionRequest => parameter => collectionFields(collectionRequest, parameter))

  def collectionFields(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      sampleSizeParameter: Option[Int]
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[String]]] = {
    Future.successful(Right({
      val dao = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
      dao.columnNames(sampleSizeParameter.getOrElse(0))
    }))
  }

  val deleteCollectionStatusEndpoint = administrateCollectionEndpoint
    .out(jsonBody[JsonResult[Boolean]])
    .summary("Delete Collection")
    .description("Delete a given Collection")
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
    .out(jsonBody[JsonResult[Boolean]])
    .summary("Clear Collection")
    .description("Delete all Document in Collection")
    .tag("Collection")
    .method(Method.DELETE)
    .name("clearCollection")
    .serverLogic(collectionRequest => _ => deleteAllInCollection(collectionRequest))

  def deleteAllInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonResult[Boolean]]] = {
    Future.successful(
      Right(
        {
          val dao    = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val result = dao.deleteAll().result()
          JsonResult(result.wasAcknowledged())
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
    .description("Aggregate in a given Collection")
    .tag("Collection")
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
    .description("Distinct for Field in a given Collection")
    .tag("Collection")
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

  override def endpoints: List[ServerEndpoint[AkkaStreams with capabilities.WebSockets, Future]] =
    List(
      collectionsEndpoint,
      getCollectionStatusEndpoint,
      getCollectionFieldsEndpoint,
      deleteCollectionStatusEndpoint,
      deleteAllEndpoint,
      aggregateEndpoint,
      distinctEndpoint
    )

}
