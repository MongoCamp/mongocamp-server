package dev.mongocamp.server.route

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.converter.MongoCampBsonConverter.{convertFields, convertToOperationMap}
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.document.{CreateDocumentEvent, DeleteDocumentEvent, UpdateDocumentEvent}
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.model.auth.AuthorizedCollectionRequest
import dev.mongocamp.server.model.{DeleteResponse, InsertResponse, UpdateRequest, UpdateResponse}
import io.circe.generic.auto._
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{Method, StatusCode}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future
import scala.jdk.CollectionConverters._
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
    .serverLogic(connection => insertList => insertManyInCollection(connection, insertList))

  def insertManyInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (List[Map[String, Any]])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), InsertResponse]] = {
    Future.successful(
      Right(
        {
          val dao                            = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val listOfDocuments                = parameter.map(map => documentFromScalaMap(convertFields(map)))
          val result                         = dao.insertMany(listOfDocuments).result()
          val listOfIds                      = result.getInsertedIds.values().asScala.map(_.asObjectId().getValue.toHexString).toList
          val insertedResult: InsertResponse = InsertResponse(result.wasAcknowledged(), listOfIds)
          EventSystem.eventStream.publish(CreateDocumentEvent(authorizedCollectionRequest.userInformation, insertedResult))
          insertedResult
        }
      )
    )
  }

  val updateManyEndpoint = writeCollectionEndpoint
    .in("documents")
    .in("many")
    .in("update")
    .in(jsonBody[UpdateRequest])
    .out(jsonBody[UpdateResponse])
    .summary("Update many in Collection")
    .description("Update many Document in given Collection")
    .tag(DocumentRoutes.apiName)
    .method(Method.PATCH)
    .name("updateMany")
    .serverLogic(collectionRequest => parameter => updateManyInCollection(collectionRequest, parameter))

  def updateManyInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: UpdateRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UpdateResponse]] = {
    Future.successful(
      Right(
        {
          val dao         = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val documentMap = parameter.document
          val filter      = convertFields(parameter.filter)
          val oldValues   = dao.find(filter).resultList()
          val result      = dao.updateMany(filter, documentFromScalaMap(convertToOperationMap(convertFields(documentMap)))).result()
          val updateResponse = UpdateResponse(
            result.wasAcknowledged(),
            Option(result.getUpsertedId).map(value => value.asObjectId().getValue.toHexString).toList,
            result.getModifiedCount,
            result.getMatchedCount
          )
          EventSystem.eventStream.publish(UpdateDocumentEvent(authorizedCollectionRequest.userInformation, updateResponse, oldValues))
          updateResponse
        }
      )
    )
  }

  val deleteManyEndpoint = writeCollectionEndpoint
    .in("documents")
    .in("many")
    .in("delete")
    .in(jsonBody[Map[String, Any]])
    .out(jsonBody[DeleteResponse])
    .summary("Delete Many in Collection")
    .description("Delete many Document in given Collection")
    .tag(DocumentRoutes.apiName)
    .method(Method.DELETE)
    .name("deleteMany")
    .serverLogic(collectionRequest => search => deleteManyInCollection(collectionRequest, search))

  def deleteManyInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: Map[String, Any]
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), DeleteResponse]] = {
    Future.successful(
      Right(
        {
          val dao            = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val deleteFilter   = convertFields(parameter)
          val oldValues      = dao.find(deleteFilter).resultList()
          val result         = dao.deleteMany(deleteFilter).result()
          val deleteResponse = DeleteResponse(result.wasAcknowledged(), result.getDeletedCount)
          EventSystem.eventStream.publish(DeleteDocumentEvent(authorizedCollectionRequest.userInformation, deleteResponse, oldValues))
          deleteResponse
        }
      )
    )
  }

  def listOfManyEndpoints(): List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = List(insertManyEndpoint, updateManyEndpoint, deleteManyEndpoint)
}
