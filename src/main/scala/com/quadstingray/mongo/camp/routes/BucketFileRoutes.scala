package com.quadstingray.mongo.camp.routes

import com.quadstingray.mongo.camp.converter.MongoCampBsonConverter
import com.quadstingray.mongo.camp.converter.MongoCampBsonConverter.{ convertFields, convertIdField }
import com.quadstingray.mongo.camp.database.MongoDatabase
import com.quadstingray.mongo.camp.database.paging.{ MongoPaginatedFilter, PaginationInfo }
import com.quadstingray.mongo.camp.exception.{ ErrorDescription, MongoCampException }
import com.quadstingray.mongo.camp.file.FileAdapterHolder
import com.quadstingray.mongo.camp.model.BucketInformation.BucketCollectionSuffix
import com.quadstingray.mongo.camp.model._
import com.quadstingray.mongo.camp.model.auth.AuthorizedCollectionRequest
import com.quadstingray.mongo.camp.routes.parameter.paging.{ Paging, PagingFunctions }
import com.sfxcode.nosql.mongo._
import io.circe.generic.auto._
import io.circe.parser.decode
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import java.util.Date
import scala.concurrent.Future

object BucketFileRoutes extends BucketBaseRoute with RoutesPlugin {
  val apiName = "File"

  val findAllEndpoint = readBucketEndpoint
    .in("files")
    .in(query[Option[String]]("filter").description("MongoDB Filter Query by Default all filter").example(Some("{}")))
    .in(query[Option[String]]("sort").description("MongoDB sorting").example(Some("{}")))
    .in(query[Option[String]]("projection").description("MongoDB projection").example(Some("{}")))
    .in(PagingFunctions.pagingParameter)
    .out(jsonBody[List[Map[String, Any]]])
    .out(PagingFunctions.pagingHeaderOutput)
    .summary("Files in Bucket")
    .description("Get Files paginated from given Bucket")
    .tag(apiName)
    .method(Method.GET)
    .name("listFiles")
    .serverLogic(bucketRequest => parameter => findAllInBucket(bucketRequest, parameter))

  def findAllInBucket(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (Option[String], Option[String], Option[String], Paging)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (List[Map[String, Any]], PaginationInfo)]] = {
    val filter: Map[String, Any]     = parameter._1.map(value => decode[Map[String, Any]](value).getOrElse(Map[String, Any]())).getOrElse(Map[String, Any]())
    val sort: Map[String, Any]       = parameter._2.map(value => decode[Map[String, Any]](value).getOrElse(Map[String, Any]())).getOrElse(Map[String, Any]())
    val projection: Map[String, Any] = parameter._3.map(value => decode[Map[String, Any]](value).getOrElse(Map[String, Any]())).getOrElse(Map[String, Any]())
    findInBucket(authorizedCollectionRequest, (MongoFindRequest(filter, sort, projection), parameter._4))
  }

  val findPostEndpoint = readBucketEndpoint
    .in("files")
    .in(
      jsonBody[MongoFindRequest].example(
        MongoFindRequest(
          Map("filename"   -> "myfile.json", "uploadDate" -> Map("$gte" -> "2022-03-15T00:00:00.000Z")),
          Map("uploadDate" -> -1),
          Map()
        )
      )
    )
    .in(PagingFunctions.pagingParameter)
    .out(jsonBody[List[Map[String, Any]]])
    .out(PagingFunctions.pagingHeaderOutput)
    .summary("Files in Bucket")
    .description("Alternative to GET Route for more complex queries and URL max. Length")
    .tag(apiName)
    .method(Method.POST)
    .name("find")
    .serverLogic(bucketRequest => parameter => findInBucket(bucketRequest, parameter))

  def findInBucket(
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
            MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection + BucketCollectionSuffix),
            convertFields(searchRequest.filter),
            searchRequest.sort,
            searchRequest.projection
          )

          val findResult = mongoPaginatedFilter.paginate(rowsPerPage, page)
          (findResult.databaseObjects.map(MongoCampBsonConverter.documentToMap), findResult.paginationInfo)
        }
      )
    )
  }

  val insertEndpoint = writeBucketEndpoint
    .in("files")
    .in(
      jsonBody[Map[String, Any]]
        .description("JSON Representation for your Document.")
        .example(Map("key1" -> "value", "key2" -> 0, "key2" -> true, "key3" -> Map("creationDate" -> new Date())))
    )
    .out(jsonBody[InsertResponse])
    .summary("Insert File")
    .description("Insert one File in given Bucket")
    .tag(apiName)
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
          val result         = dao.insertOne(documentFromScalaMap(convertFields(parameter))).result()
          val insertedResult = InsertResponse(result.wasAcknowledged(), List(result.getInsertedId.asObjectId().getValue.toHexString))
          insertedResult
        }
      )
    )
  }

  val getFileInfosEndpoint = readBucketEndpoint
    .in("files")
    .in(path[String]("fileId").description("fileId to read"))
    .out(jsonBody[Map[String, Any]])
    .summary("FileInformation from Bucket")
    .description("Get one FileInformation from given Bucket")
    .tag(apiName)
    .method(Method.GET)
    .name("getDocument")
    .serverLogic(collectionRequest => parameter => findById(collectionRequest, parameter))

  def findById(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Map[String, Any]]] = {
    Future.successful(
      Right({
        val result = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection + BucketCollectionSuffix).findById(parameter).resultOption()
        result.getOrElse(throw MongoCampException("could not find document", StatusCode.NotFound))
      })
    )
  }

  val getFileEndpoint = readBucketEndpoint
    .in("files")
    .in(path[String]("fileId").description("FileId to read"))
    .in("file")
    .out(jsonBody[Map[String, Any]])
    .summary("FileInformation from Bucket")
    .description("Get one FileInformation from given Bucket")
    .tag(apiName)
    .method(Method.GET)
    .name("getFileInformation")
    .serverLogic(collectionRequest => parameter => getFileById(collectionRequest, parameter))

  def getFileById(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Map[String, Any]]] = {
    Future.successful(
      Right({
        val result = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection).findById(parameter).resultOption()
        result.getOrElse(throw MongoCampException("could not find document", StatusCode.NotFound))
      })
    )
  }

  val deleteFileEndpoint = writeBucketEndpoint
    .in("files")
    .in(path[String]("fileId").description("fileId to delete"))
    .out(jsonBody[DeleteResponse])
    .summary("Delete File from Bucket")
    .description("Delete one File from given Bucket")
    .tag(apiName)
    .method(Method.DELETE)
    .name("deleteFile")
    .serverLogic(collectionRequest => parameter => deleteById(collectionRequest, parameter))

  def deleteById(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), DeleteResponse]] = {
    Future.successful(
      Right({
        val result = MongoDatabase.databaseProvider
          .dao(authorizedCollectionRequest.collection + BucketCollectionSuffix)
          .deleteOne(Map("_id" -> convertIdField(parameter)))
          .result()
        FileAdapterHolder.handler.deleteFile(authorizedCollectionRequest.collection, parameter)
        val deleteResponse = DeleteResponse(result.wasAcknowledged(), result.getDeletedCount)
        deleteResponse
      })
    )
  }

  override def endpoints: List[ServerEndpoint[AkkaStreams with capabilities.WebSockets, Future]] =
    List(findAllEndpoint, findPostEndpoint) ++
      List(insertEndpoint, getFileInfosEndpoint, getFileEndpoint, deleteFileEndpoint)

}
