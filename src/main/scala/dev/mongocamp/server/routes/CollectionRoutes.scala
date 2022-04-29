package dev.mongocamp.server.routes

import com.sfxcode.nosql.mongo._
import com.sfxcode.nosql.mongo.bson.BsonConverter
import com.sfxcode.nosql.mongo.database.CollectionStatus
import com.sfxcode.nosql.mongo.database.DatabaseProvider.CollectionSeparator
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.database.paging.{ MongoPaginatedAggregation, PaginationInfo }
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.model.BucketInformation.BucketCollectionSuffix
import dev.mongocamp.server.model.auth.{ AuthorizedCollectionRequest, UserInformation }
import dev.mongocamp.server.model.{ JsonResult, JsonSchema, MongoAggregateRequest, SchemaAnalysis }
import dev.mongocamp.server.routes.parameter.paging.{ Paging, PagingFunctions }
import dev.mongocamp.server.service.{ AggregationService, SchemaService }
import io.circe.generic.auto._
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.{ EndpointInput, _ }

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
      val mongoDatabase = if (authorizedCollectionRequest.collection.contains(CollectionSeparator)) {
        MongoDatabase.databaseProvider.database(authorizedCollectionRequest.collection.split(CollectionSeparator).head)
      }
      else {
        MongoDatabase.databaseProvider.database()
      }

      val collection = authorizedCollectionRequest.collection.split(CollectionSeparator).last

      val result = mongoDatabase
        .runCommand(Map("collStats" -> collection))
        .map(document => CollectionStatus(document))
        .result()

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

  private val sampleSize: EndpointInput.Query[Option[Int]] =
    query[Option[Int]]("sampleSize").example(Some(5000)).description("Use sample size greater 0 (e.g. 5000) for better performance on big collections")

  val getSchemaEndpoint = readCollectionEndpoint
    .in("schema")
    .in(sampleSize)
    .in(query[Int]("deepth").default(3).description("How deep should the objects extracted"))
    .out(jsonBody[JsonSchema])
    .summary("Collection Fields")
    .description("List the Fields in a given collection")
    .tag("Collection")
    .method(Method.GET)
    .name("getJsonSchema")
    .serverLogic(collectionRequest => parameter => detectSchema(collectionRequest, parameter))

  def detectSchema(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (Option[Int], Int)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonSchema]] = {
    Future.successful(Right({
      val deepth = parameter._2
      val sample = parameter._1
      SchemaService.detectSchema(authorizedCollectionRequest, deepth, sample)
    }))
  }

  val getSchemaAnalysisEndpoint = readCollectionEndpoint
    .in("schema")
    .in("analysis")
    .in(sampleSize)
    .in(query[Int]("deepth").default(3).description("How deep should the objects extracted"))
    .out(jsonBody[SchemaAnalysis])
    .summary("Collection Fields")
    .description("List the Fields in a given collection")
    .tag("Collection")
    .method(Method.GET)
    .name("getSchemaAnalysis")
    .serverLogic(collectionRequest => parameter => detectSchemaAnalysis(collectionRequest, parameter))

  def detectSchemaAnalysis(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (Option[Int], Int)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), SchemaAnalysis]] = {
    Future.successful(Right({
      val deepth = parameter._2
      val sample = parameter._1
      SchemaService.analyzeSchema(authorizedCollectionRequest, deepth, sample)
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

          AggregationService.paginatedAggregation(authorizedCollectionRequest, mongoAggregateRequest, rowsPerPage, page)

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
      getSchemaEndpoint,
      getSchemaAnalysisEndpoint,
      deleteCollectionStatusEndpoint,
      deleteAllEndpoint,
      aggregateEndpoint,
      distinctEndpoint
    )

}
