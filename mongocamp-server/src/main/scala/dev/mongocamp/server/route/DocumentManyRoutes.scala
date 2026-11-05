package dev.mongocamp.server.route

import com.mongodb.MongoBulkWriteException
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.converter.LuceneQueryTools
import dev.mongocamp.server.converter.MongoCampBsonConverter.convertFields
import dev.mongocamp.server.converter.MongoCampBsonConverter.convertToOperationMap
import dev.mongocamp.server.converter.MongoCampBsonConverter.idToString
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.event.document.CreateDocumentEvent
import dev.mongocamp.server.event.document.DeleteDocumentEvent
import dev.mongocamp.server.event.document.UpdateDocumentEvent
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.exception.DatabaseErrorDescription
import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.auth.AuthorizedCollectionRequest
import dev.mongocamp.server.model.DeleteResponse
import dev.mongocamp.server.model.InsertError
import dev.mongocamp.server.model.InsertResponse
import dev.mongocamp.server.model.UpdateRequest
import dev.mongocamp.server.model.UpdateResponse
import org.apache.lucene.search.Query
import org.bson.types.ObjectId
import org.mongodb.scala.bson.Document
import org.mongodb.scala.model.InsertManyOptions
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import sttp.capabilities.pekko.PekkoStreams
import sttp.capabilities.WebSockets
import sttp.model.Method
import sttp.model.StatusCode
import sttp.tapir.Codec
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
    .in(
      query[Boolean]("ordered")
        .description(
          "true (default): the insert stops at the first document that fails (answer 409 for a duplicate key). false: all documents that can be stored are stored, the others are listed in `errors` of the answer."
        )
        .default(true)
    )
    .out(jsonBody[InsertResponse])
    .summary("Insert many Documents")
    .description("Insert many documents in given Collection")
    .tag(DocumentRoutes.apiName)
    .method(Method.PUT)
    .name("insertMany")
    .serverLogic(
      connection => insertList => insertManyInCollection(connection, insertList._1, insertList._2)
    )

  def insertManyInCollection(
    authorizedCollectionRequest: AuthorizedCollectionRequest,
    parameter: List[Map[String, Any]],
    ordered: Boolean = true
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), InsertResponse]] = {
    Future.successful(Right {
      if (parameter.isEmpty) {
        throw MongoCampException.badRequestException("the list of documents to insert is empty", ErrorCodes.emptyBatch)
      }
      val dao = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
      val listOfDocuments = parameter.map(
        map => documentFromScalaMap(convertFields(map))
      )
      val insertedResult: InsertResponse = if (ordered) insertOrdered(dao, listOfDocuments) else insertUnordered(dao, listOfDocuments)
      EventSystem.publish(CreateDocumentEvent(authorizedCollectionRequest.userInformation, insertedResult))
      insertedResult
    })
  }

  private def insertOrdered(dao: MongoDAO[Document], documents: List[Document]): InsertResponse = {
    val result    = dao.insertMany(documents).result()
    val listOfIds = result.getInsertedIds.values().asScala.map(idToString).toList
    InsertResponse(result.wasAcknowledged(), listOfIds, Some(listOfIds.size.toLong), Some(List()))
  }

  // the ids are set here, the database does not report the ids of the documents that were stored when others failed
  private def insertUnordered(dao: MongoDAO[Document], documents: List[Document]): InsertResponse = {
    val withIds = documents.map(
      document => if (document.contains("_id")) document else document + ("_id" -> new ObjectId())
    )
    try {
      dao.insertMany(withIds, new InsertManyOptions().ordered(false)).result()
      val ids = withIds.map(document => idToString(document.get("_id").get))
      InsertResponse(wasAcknowledged = true, ids, Some(ids.size.toLong), Some(List()))
    }
    catch {
      case ex: Exception =>
        DatabaseErrorDescription.findDatabaseException(ex) match {
          case Some(bulk: MongoBulkWriteException) if !bulk.getWriteErrors.isEmpty =>
            val errors = bulk.getWriteErrors.asScala.toList.map(
              error => InsertError(error.getIndex, error.getCode, DatabaseErrorDescription.removeInternalNames(error.getMessage))
            )
            val failed = errors.map(_.index).toSet
            val ids    = withIds.zipWithIndex.filter(entry => !failed.contains(entry._2)).map(entry => idToString(entry._1.get("_id").get))
            InsertResponse(wasAcknowledged = true, ids, Some(ids.size.toLong), Some(errors))
          case _ => throw ex
        }
    }
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

  private val luceneFilterInput =
    query[Option[Query]]("filter")(using Codec.listHeadOption(LuceneQueryTools.filterCodec))
      .description("Filter Query like on the list of documents, as an alternative to the filter of the request body. Use only one of both.")
      .default(None)

  // the filter of the request body or the Lucene filter of the query, not both: a filter that is silently ignored would touch more documents than intended
  private def effectiveFilter(bodyFilter: Map[String, Any], luceneFilter: Option[Query]): Map[String, Any] = {
    if (luceneFilter.isDefined && bodyFilter.nonEmpty) {
      throw MongoCampException.badRequestException(
        "send the filter in the request body or as query parameter filter, not both",
        ErrorCodes.ambiguousFilter
      )
    }
    if (luceneFilter.isDefined) LuceneQueryTools.toFilterMap(luceneFilter) else bodyFilter
  }

  val updateManyEndpoint = writeCollectionEndpoint
    .in("documents")
    .in("many")
    .in("update")
    .in(jsonBody[UpdateRequest])
    .in(query[Boolean]("matchAll").description("Allow an empty filter, that matches all documents of the collection").default(false))
    .in(luceneFilterInput)
    .out(jsonBody[UpdateResponse])
    .summary("Update many in Collection")
    .description("Update many Document in given Collection")
    .tag(DocumentRoutes.apiName)
    .method(Method.PATCH)
    .name("updateMany")
    .serverLogic(
      collectionRequest => parameter => updateManyInCollection(collectionRequest, parameter._1, parameter._2, parameter._3)
    )

  def updateManyInCollection(
    authorizedCollectionRequest: AuthorizedCollectionRequest,
    parameter: UpdateRequest,
    matchAll: Boolean = false,
    luceneFilter: Option[Query] = None
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UpdateResponse]] = {
    Future.successful(Right {
      val dao         = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
      val documentMap = parameter.document
      val filter      = convertFields(effectiveFilter(parameter.filter, luceneFilter))
      rejectEmptyFilter(filter, matchAll)
      // on a match all operation this loads every document for the event
      val oldValues = dao.find(filter).resultList()
      val result    = dao.updateMany(filter, documentFromScalaMap(convertToOperationMap(convertFields(documentMap)))).result()
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
    .in(luceneFilterInput)
    .out(jsonBody[DeleteResponse])
    .summary("Delete Many in Collection")
    .description("Delete many Document in given Collection")
    .tag(DocumentRoutes.apiName)
    .method(Method.DELETE)
    .name("deleteMany")
    .serverLogic(
      collectionRequest => search => deleteManyInCollection(collectionRequest, search._1, search._2, search._3)
    )

  def deleteManyInCollection(
    authorizedCollectionRequest: AuthorizedCollectionRequest,
    parameter: Map[String, Any],
    matchAll: Boolean = false,
    luceneFilter: Option[Query] = None
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), DeleteResponse]] = {
    Future.successful(Right {
      val dao          = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
      val deleteFilter = convertFields(effectiveFilter(parameter, luceneFilter))
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
