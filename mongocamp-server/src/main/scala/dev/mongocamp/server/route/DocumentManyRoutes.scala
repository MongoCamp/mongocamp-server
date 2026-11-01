package dev.mongocamp.server.route

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.converter.MongoCampBsonConverter.convertFields
import dev.mongocamp.server.converter.MongoCampBsonConverter.convertToOperationMap
import dev.mongocamp.server.converter.MongoCampBsonConverter.idToString
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.event.document.CreateDocumentEvent
import dev.mongocamp.server.event.document.DeleteDocumentEvent
import dev.mongocamp.server.event.document.UpdateDocumentEvent
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.auth.AuthorizedCollectionRequest
import dev.mongocamp.server.model.DeleteResponse
import dev.mongocamp.server.model.InsertResponse
import dev.mongocamp.server.model.UpdateRequest
import dev.mongocamp.server.model.UpdateResponse
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import sttp.capabilities.pekko.PekkoStreams
import sttp.capabilities.WebSockets
import sttp.model.Method
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.query
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.stringToPath
object DocumentManyRoutes extends CollectionBaseRoute {

  val insertManyEndpoint = writeCollectionEndpoint
    .in("documents")
    .in("many")
    .in("insert")
    .in(jsonBody[List[Map[String, Any]]])
    .out(jsonBody[InsertResponse])
    .summary("Insert many Documents")
    .description("Insert many documents in given Collection")
    .tag(DocumentRoutes.apiName)
    .method(Method.PUT)
    .name("insertMany")
    .serverLogic(
      connection => insertList => insertManyInCollection(connection, insertList)
    )

  def insertManyInCollection(
    authorizedCollectionRequest: AuthorizedCollectionRequest,
    parameter: (List[Map[String, Any]])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), InsertResponse]] = {
    Future.successful(Right {
      if (parameter.isEmpty) {
        throw MongoCampException.badRequestException("the list of documents to insert is empty", ErrorCodes.emptyBatch)
      }
      val dao = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
      val listOfDocuments = parameter.map(
        map => documentFromScalaMap(convertFields(map))
      )
      val result                         = dao.insertMany(listOfDocuments).result()
      val listOfIds                      = result.getInsertedIds.values().asScala.map(idToString).toList
      val insertedResult: InsertResponse = InsertResponse(result.wasAcknowledged(), listOfIds)
      EventSystem.publish(CreateDocumentEvent(authorizedCollectionRequest.userInformation, insertedResult))
      insertedResult
    })
  }

  // an empty filter matches every document, for an HTTP API that is too easy to do by accident (a filter built from an empty list of ids)
  private def rejectEmptyFilter(filter: Map[String, Any], matchAll: Boolean): Unit = {
    if (filter.isEmpty && !matchAll) {
      throw MongoCampException.badRequestException(
        "the filter is empty and would match all documents of the collection, send the query parameter matchAll=true if that is intended",
        ErrorCodes.emptyFilter
      )
    }
  }

  val updateManyEndpoint = writeCollectionEndpoint
    .in("documents")
    .in("many")
    .in("update")
    .in(jsonBody[UpdateRequest])
    .in(query[Boolean]("matchAll").description("Allow an empty filter, that matches all documents of the collection").default(false))
    .out(jsonBody[UpdateResponse])
    .summary("Update many in Collection")
    .description("Update many Document in given Collection")
    .tag(DocumentRoutes.apiName)
    .method(Method.PATCH)
    .name("updateMany")
    .serverLogic(
      collectionRequest => parameter => updateManyInCollection(collectionRequest, parameter._1, parameter._2)
    )

  def updateManyInCollection(
    authorizedCollectionRequest: AuthorizedCollectionRequest,
    parameter: UpdateRequest,
    matchAll: Boolean = false
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UpdateResponse]] = {
    Future.successful(Right {
      val dao         = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
      val documentMap = parameter.document
      val filter      = convertFields(parameter.filter)
      rejectEmptyFilter(filter, matchAll)
      // on a match all operation this loads every document for the event
      val oldValues   = dao.find(filter).resultList()
      val result      = dao.updateMany(filter, documentFromScalaMap(convertToOperationMap(convertFields(documentMap)))).result()
      val updateResponse = UpdateResponse(
        result.wasAcknowledged(),
        Option(result.getUpsertedId)
          .map(
            value => idToString(value)
          )
          .toList,
        result.getModifiedCount,
        result.getMatchedCount
      )
      EventSystem.publish(UpdateDocumentEvent(authorizedCollectionRequest.userInformation, updateResponse, oldValues))
      updateResponse
    })
  }

  val deleteManyEndpoint = writeCollectionEndpoint
    .in("documents")
    .in("many")
    .in("delete")
    .in(jsonBody[Map[String, Any]])
    .in(query[Boolean]("matchAll").description("Allow an empty filter, that matches all documents of the collection").default(false))
    .out(jsonBody[DeleteResponse])
    .summary("Delete Many in Collection")
    .description("Delete many Document in given Collection")
    .tag(DocumentRoutes.apiName)
    .method(Method.DELETE)
    .name("deleteMany")
    .serverLogic(
      collectionRequest => search => deleteManyInCollection(collectionRequest, search._1, search._2)
    )

  def deleteManyInCollection(
    authorizedCollectionRequest: AuthorizedCollectionRequest,
    parameter: Map[String, Any],
    matchAll: Boolean = false
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), DeleteResponse]] = {
    Future.successful(Right {
      val dao            = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
      val deleteFilter   = convertFields(parameter)
      rejectEmptyFilter(deleteFilter, matchAll)
      // on a match all operation this loads every document for the event
      val oldValues      = dao.find(deleteFilter).resultList()
      val result         = dao.deleteMany(deleteFilter).result()
      val deleteResponse = DeleteResponse(result.wasAcknowledged(), result.getDeletedCount)
      EventSystem.publish(DeleteDocumentEvent(authorizedCollectionRequest.userInformation, deleteResponse, oldValues))
      deleteResponse
    })
  }

  def listOfManyEndpoints(): List[ServerEndpoint[PekkoStreams with WebSockets, Future]] = List(insertManyEndpoint, updateManyEndpoint, deleteManyEndpoint)
}
