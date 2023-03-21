package dev.mongocamp.server.route

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.converter.MongoCampBsonConverter
import dev.mongocamp.server.converter.MongoCampBsonConverter.{ convertFields, convertIdField, convertToOperationMap }
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.database.paging.{ MongoPaginatedFilter, PaginationInfo }
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.document.{ CreateDocumentEvent, DeleteDocumentEvent, UpdateDocumentEvent }
import dev.mongocamp.server.exception.{ ErrorDescription, MongoCampException }
import dev.mongocamp.server.model.auth.AuthorizedCollectionRequest
import dev.mongocamp.server.model.{ DeleteResponse, InsertResponse, MongoFindRequest, UpdateResponse }
import dev.mongocamp.server.plugin.RoutesPlugin
import dev.mongocamp.server.route.parameter.paging.{ Paging, PagingFunctions }
import io.circe.generic.auto._
import io.circe.parser.decode
import org.bson.types.ObjectId
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import java.util.Date
import scala.concurrent.Future

object DocumentRoutes extends CollectionBaseRoute with RoutesPlugin {
  val apiName = "Document"

  val findAllEndpoint = readCollectionEndpoint
    .in("documents")
    .in(query[Option[String]]("filter").description("MongoDB Filter Query by Default all filter").example(Some("{}")))
    .in(query[Option[String]]("sort").description("MongoDB sorting").example(Some("{}")))
    .in(query[Option[String]]("projection").description("MongoDB projection").example(Some("{}")))
    .in(PagingFunctions.pagingParameter)
    .out(jsonBody[List[Map[String, Any]]])
    .out(PagingFunctions.pagingHeaderOutput)
    .summary("Documents in Collection")
    .description("Get Documents paginated from given Collection")
    .tag(apiName)
    .method(Method.GET)
    .name("listDocuments")
    .serverLogic(collectionRequest => parameter => findAllInCollection(collectionRequest, parameter))

  def findAllInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (Option[String], Option[String], Option[String], Paging)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (List[Map[String, Any]], PaginationInfo)]] = {
    val filter: Map[String, Any]     = parameter._1.map(value => decode[Map[String, Any]](value).getOrElse(Map[String, Any]())).getOrElse(Map[String, Any]())
    val sort: Map[String, Any]       = parameter._2.map(value => decode[Map[String, Any]](value).getOrElse(Map[String, Any]())).getOrElse(Map[String, Any]())
    val projection: Map[String, Any] = parameter._3.map(value => decode[Map[String, Any]](value).getOrElse(Map[String, Any]())).getOrElse(Map[String, Any]())
    findInCollection(authorizedCollectionRequest, (MongoFindRequest(filter, sort, projection), parameter._4))
  }

  val findPostEndpoint = readCollectionEndpoint
    .in("documents")
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
    .summary("Documents in Collection")
    .description("Alternative to GET Route for more complex queries and URL max. Length")
    .tag(apiName)
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

  val insertEndpoint = writeCollectionEndpoint
    .in("documents")
    .in(
      jsonBody[Map[String, Any]]
        .description("JSON Representation for your Document.")
        .example(Map("key1" -> "value", "key2" -> 0, "key2" -> true, "key3" -> Map("creationDate" -> new Date())))
    )
    .out(jsonBody[InsertResponse])
    .summary("Insert Document")
    .description("Insert one Document in given Collection")
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
          EventSystem.eventStream.publish(CreateDocumentEvent(authorizedCollectionRequest.userInformation, insertedResult))
          insertedResult
        }
      )
    )
  }

  val getDocumentEndpoint = readCollectionEndpoint
    .in("documents")
    .in(path[String]("documentId").description("DocumentId to read"))
    .out(jsonBody[Map[String, Any]])
    .summary("Document from Collection")
    .description("Get one Document from given Collection")
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
        val result = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection).findById(parameter).resultOption()
        result.getOrElse(throw MongoCampException("could not find document", StatusCode.NotFound))
      })
    )
  }

  val deleteDocumentEndpoint = writeCollectionEndpoint
    .in("documents")
    .in(path[String]("documentId").description("DocumentId to delete"))
    .out(jsonBody[DeleteResponse])
    .summary("Delete Document from Collection")
    .description("Delete one Document from given Collection")
    .tag(apiName)
    .method(Method.DELETE)
    .name("delete")
    .serverLogic(collectionRequest => parameter => deleteById(collectionRequest, parameter))

  def deleteById(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), DeleteResponse]] = {
    Future.successful(
      Right({
        val filter         = Map("_id" -> convertIdField(parameter))
        val oldValues      = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection).find(filter).resultList()
        val result         = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection).deleteOne(filter).result()
        val deleteResponse = DeleteResponse(result.wasAcknowledged(), result.getDeletedCount)
        EventSystem.eventStream.publish(DeleteDocumentEvent(authorizedCollectionRequest.userInformation, deleteResponse, oldValues))
        deleteResponse
      })
    )
  }

  val updateSingleDocumentEndpoint = writeCollectionEndpoint
    .in("documents")
    .in(path[String]("documentId").description("DocumentId to update"))
    .in(jsonBody[Map[String, Any]])
    .out(jsonBody[UpdateResponse])
    .summary("Update Document in Collection")
    .description("'Replace' one Document with the new document from Request in Collection")
    .tag(apiName)
    .method(Method.PATCH)
    .name("update")
    .serverLogic(collectionRequest => parameter => replaceInCollection(collectionRequest, parameter))

  def replaceInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (String, Map[String, Any])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UpdateResponse]] = {
    Future.successful(
      Right(
        {
          val dao                = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val documentMap        = parameter._2
          val originalDocumentId = convertIdField(parameter._1)
          val filter             = Map[String, Any]("_id" -> originalDocumentId)
          val oldValues          = dao.find(filter).resultList()
          val result             = dao.replaceOne(filter, documentFromScalaMap(documentMap)).result()
          val maybeValue: Option[ObjectId] = if (result.getModifiedCount == 1 && result.getUpsertedId == null) {
            Some(originalDocumentId)
          }
          else {
            Option(result.getUpsertedId).map(value => value.asObjectId().getValue)
          }
          val updateResponse = UpdateResponse(
            result.wasAcknowledged(),
            maybeValue.map(value => value.toHexString).toList,
            result.getModifiedCount,
            result.getMatchedCount
          )
          EventSystem.eventStream.publish(UpdateDocumentEvent(authorizedCollectionRequest.userInformation, updateResponse, oldValues))
          updateResponse
        }
      )
    )
  }

  val updateDocumentFieldsEndpoint = writeCollectionEndpoint
    .in("documents")
    .in(path[String]("documentId").description("DocumentId to update"))
    .in("partial")
    .in(jsonBody[Map[String, Any]])
    .out(jsonBody[UpdateResponse])
    .summary("Update Document Parts in Collection")
    .description("Update the document Parts with the values from the Request")
    .tag(apiName)
    .method(Method.PATCH)
    .name("updateDocumentPartial")
    .serverLogic(collectionRequest => parameter => updateFieldsInCollection(collectionRequest, parameter))

  def updateFieldsInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (String, Map[String, Any])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UpdateResponse]] = {
    Future.successful(
      Right(
        {
          val dao = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)

          val document = convertToOperationMap(parameter._2)

          val originalDocumentId = convertIdField(parameter._1)
          val filter             = Map[String, Any]("_id" -> originalDocumentId)
          val oldValues          = dao.find(filter).resultList()
          val result             = dao.updateOne(filter, documentFromScalaMap(document)).result()
          val maybeValue: Option[ObjectId] = if (result.getModifiedCount == 1 && result.getUpsertedId == null) {
            Some(originalDocumentId)
          }
          else {
            Option(result.getUpsertedId).map(value => value.asObjectId().getValue)
          }

          val updateResponse = UpdateResponse(
            result.wasAcknowledged(),
            maybeValue.map(value => value.toHexString).toList,
            result.getModifiedCount,
            result.getMatchedCount
          )
          EventSystem.eventStream.publish(UpdateDocumentEvent(authorizedCollectionRequest.userInformation, updateResponse, oldValues))
          updateResponse
        }
      )
    )
  }

  override def endpoints: List[ServerEndpoint[AkkaStreams with capabilities.WebSockets, Future]] =
    List(findAllEndpoint, findPostEndpoint) ++
      DocumentManyRoutes.listOfManyEndpoints() ++
      List(insertEndpoint, getDocumentEndpoint, updateSingleDocumentEndpoint, updateDocumentFieldsEndpoint, deleteDocumentEndpoint)

}
