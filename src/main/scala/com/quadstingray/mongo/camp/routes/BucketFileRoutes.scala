package com.quadstingray.mongo.camp.routes

import better.files.File
import com.quadstingray.mongo.camp.converter.MongoCampBsonConverter.{ convertFields, convertIdField }
import com.quadstingray.mongo.camp.database.paging.{ MongoPaginatedFilter, PaginationInfo }
import com.quadstingray.mongo.camp.database.{ FileInformationDao, MongoDatabase }
import com.quadstingray.mongo.camp.exception.{ ErrorDescription, MongoCampException }
import com.quadstingray.mongo.camp.file.FileAdapterHolder
import com.quadstingray.mongo.camp.model.BucketInformation.BucketCollectionSuffix
import com.quadstingray.mongo.camp.model._
import com.quadstingray.mongo.camp.model.auth.AuthorizedCollectionRequest
import com.quadstingray.mongo.camp.routes.file.FileFunctions.fileResult
import com.quadstingray.mongo.camp.routes.file.FileResult
import com.quadstingray.mongo.camp.routes.parameter.paging.{ Paging, PagingFunctions }
import com.sfxcode.nosql.mongo._
import io.circe.generic.auto._
import io.circe.parser.decode
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, Part, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object BucketFileRoutes extends BucketBaseRoute with RoutesPlugin {
  val apiName = "File"

  val findAllEndpoint = readBucketEndpoint
    .in("files")
    .in(query[Option[String]]("filter").description("MongoDB Filter Query by Default all filter").example(Some("{}")))
    .in(query[Option[String]]("sort").description("MongoDB sorting").example(Some("{}")))
    .in(query[Option[String]]("projection").description("MongoDB projection").example(Some("{}")))
    .in(PagingFunctions.pagingParameter)
    .out(jsonBody[List[FileInformation]])
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
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (List[FileInformation], PaginationInfo)]] = {
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
    .out(jsonBody[List[FileInformation]])
    .out(PagingFunctions.pagingHeaderOutput)
    .summary("Files in Bucket")
    .description("Alternative to GET Route for more complex queries and URL max. Length")
    .tag(apiName)
    .method(Method.POST)
    .name("findFiles")
    .serverLogic(bucketRequest => parameter => findInBucket(bucketRequest, parameter))

  def findInBucket(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (MongoFindRequest, Paging)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (List[FileInformation], PaginationInfo)]] = {
    Future.successful(
      Right(
        {
          val searchRequest = parameter._1
          val pagingInfo    = parameter._2
          val rowsPerPage   = pagingInfo.rowsPerPage.getOrElse(PagingFunctions.DefaultRowsPerPage)
          val page          = pagingInfo.page.getOrElse(1L)

          val mongoPaginatedFilter = MongoPaginatedFilter(
            FileInformationDao(authorizedCollectionRequest.collection),
            convertFields(searchRequest.filter),
            searchRequest.sort,
            searchRequest.projection
          )

          val findResult = mongoPaginatedFilter.paginate(rowsPerPage, page)
          (findResult.databaseObjects.map(dbFile => FileInformation(dbFile)), findResult.paginationInfo)
        }
      )
    )
  }

  val insertEndpoint = writeBucketEndpoint
    .in("files")
    .in(
      multipartBody[FileUploadForm]
        .example(
          FileUploadForm(
            Part("testFile", File.newTemporaryFile("prefix", "suffix").toJava),
            "{\"metakey\":\"value1\"}"
          )
        )
        .description("Request um neue Einträge mittels einer Datei anzulegen")
    )
    .in(query[Option[String]]("fileName").description("override filename of uploaded file"))
    .out(jsonBody[InsertResponse])
    .summary("Insert File")
    .description("Insert one File in given Bucket")
    .tag(apiName)
    .method(Method.PUT)
    .name("insertFile")
    .serverLogic(login => insert => insertInBucket(login, insert))

  def insertInBucket(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (FileUploadForm, Option[String])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), InsertResponse]] = {
    Future.successful(
      Right(
        {
          val metadata: Map[String, Any] = convertFields(decode[Map[String, Any]](parameter._1.metaData).getOrElse(Map[String, Any]()))
          val uploadedFile               = better.files.File(parameter._1.file.body.getPath)
          val fileName                   = parameter._2.getOrElse(parameter._1.file.otherDispositionParams.getOrElse("filename", uploadedFile.name))
          if (FileAdapterHolder.isGridfsHolder) {
            object FilesDAO extends GridFSDAO(MongoDatabase.databaseProvider, authorizedCollectionRequest.collection)
            val result         = FilesDAO.uploadFile(fileName, uploadedFile, documentFromScalaMap(metadata)).result()
            val insertedResult = InsertResponse(wasAcknowledged = true, List(result.toHexString))
            insertedResult
          }
          else {
            throw MongoCampException("not implemented at this point", StatusCode.NotImplemented)
          }
        }
      )
    )
  }

  val getFileInfosEndpoint = readBucketEndpoint
    .in("files")
    .in(path[String]("fileId").description("fileId to read"))
    .out(jsonBody[FileInformation])
    .summary("FileInformation from Bucket")
    .description("Get one FileInformation from given Bucket")
    .tag(apiName)
    .method(Method.GET)
    .name("getFileInformation")
    .serverLogic(collectionRequest => parameter => findById(collectionRequest, parameter))

  def findById(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), FileInformation]] = {
    Future.successful(
      Right({
        getFileInformation(authorizedCollectionRequest.collection, parameter)
      })
    )
  }

  def getFileInformation(bucketName: String, fileId: String): FileInformation = {
    val result = FileInformationDao(bucketName).findById(fileId).resultOption()
    result.map(dbFile => FileInformation(dbFile)).getOrElse(throw MongoCampException("could not find document", StatusCode.NotFound))
  }

  val getFileEndpoint = readBucketEndpoint
    .in("files")
    .in(path[String]("fileId").description("FileId to read"))
    .in("file")
    .out(fileResult)
    .summary("File from Bucket")
    .description("Get File from given Bucket")
    .tag(apiName)
    .method(Method.GET)
    .name("getFile")
    .serverLogic(collectionRequest => parameter => getFileById(collectionRequest, parameter))

  def getFileById(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), FileResult]] = {
    Future.successful(
      Right({
        val fileInformation = getFileInformation(authorizedCollectionRequest.collection, parameter)
        val file            = FileAdapterHolder.handler.getFile(authorizedCollectionRequest.collection, parameter)
        FileResult(file, Some(fileInformation.filename))
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
