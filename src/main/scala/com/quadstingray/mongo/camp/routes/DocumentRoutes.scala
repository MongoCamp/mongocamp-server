package com.quadstingray.mongo.camp.routes

import com.quadstingray.mongo.camp.database.MongoDatabase
import com.quadstingray.mongo.camp.database.paging.PaginationInfo
import com.quadstingray.mongo.camp.exception.{ ErrorDescription, MongoCampException }
import com.quadstingray.mongo.camp.model._
import com.quadstingray.mongo.camp.model.auth.AuthorizedCollectionRequest
import com.quadstingray.mongo.camp.routes.ReadRoutes.findInCollection
import com.quadstingray.mongo.camp.routes.parameter.paging.{ Paging, PagingFunctions }
import com.sfxcode.nosql.mongo._
import io.circe.generic.auto._
import org.bson.types.ObjectId
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import java.util.Date
import scala.collection.mutable
import scala.concurrent.Future

object DocumentRoutes extends RoutesPlugin {

  val findAllEndpoint = readCollectionEndpoint
    .in("documents")
    .in(PagingFunctions.pagingParameter)
    .out(jsonBody[List[Map[String, Any]]])
    .out(PagingFunctions.pagingHeaderOutput)
    .summary("Documents in Collection")
    .description("Get Documents paginated from MongoDatabase Collection")
    .tag("Documents")
    .method(Method.GET)
    .name("documentsList")
    .serverLogic(collectionRequest => parameter => findAllInCollection(collectionRequest, parameter))

  def findAllInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (Paging)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (List[Map[String, Any]], PaginationInfo)]] = {
    findInCollection(authorizedCollectionRequest, (MongoFindRequest(Map(), Map(), Map()), parameter))
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
    .description("Insert one Document in Collection")
    .tag("Documents")
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
          val result         = dao.insertOne(documentFromScalaMap(parameter)).result()
          val insertedResult = InsertResponse(result.wasAcknowledged(), List(result.getInsertedId.asObjectId().getValue.toHexString))
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
    .description("Get one Document from Collection")
    .tag("Documents")
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
    .description("Delete one Document from Collection")
    .tag("Documents")
    .method(Method.DELETE)
    .name("deleteDocument")
    .serverLogic(collectionRequest => parameter => deleteById(collectionRequest, parameter))

  def deleteById(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), DeleteResponse]] = {
    Future.successful(
      Right({
        val result = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection).deleteOne(Map("_id" -> new ObjectId(parameter))).result()
        val deleteResponse = DeleteResponse(result.wasAcknowledged(), result.getDeletedCount)
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
    .tag("Documents")
    .method(Method.PATCH)
    .name("updateDocument")
    .serverLogic(collectionRequest => parameter => replaceInCollection(collectionRequest, parameter))

  def replaceInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (String, Map[String, Any])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UpdateResponse]] = {
    Future.successful(
      Right(
        {
          val dao         = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val documentMap = parameter._2
          val result      = dao.replaceOne(Map[String, Any]("_id" -> new ObjectId(parameter._1)), documentFromScalaMap(documentMap)).result()
          val insertedResult = UpdateResponse(
            result.wasAcknowledged(),
            Option(result.getUpsertedId).map(value => value.asObjectId().getValue.toHexString).toList,
            result.getModifiedCount,
            result.getMatchedCount
          )
          insertedResult
        }
      )
    )
  }

  val updateDocumentFieldsEndpoint = writeCollectionEndpoint
    .in("documents")
    .in(path[String]("documentId").description("DocumentId to update"))
    .in("partitial")
    .in(jsonBody[Map[String, Any]])
    .out(jsonBody[UpdateResponse])
    .summary("Update Document Parts in Collection")
    .description("Update the document Parts with the values from the Request")
    .tag("Documents")
    .method(Method.PATCH)
    .name("updateDocumentPartitial")
    .serverLogic(collectionRequest => parameter => updateFieldsInCollection(collectionRequest, parameter))

  def updateFieldsInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (String, Map[String, Any])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UpdateResponse]] = {
    Future.successful(
      Right(
        {
          val dao = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)

          val document = mutable.Map[String, Any]()

          parameter._2.foreach(element => {
            if (element._1.startsWith("$")) {
              document.put(element._1, element._2)
            }
            else {
              if (element._2 == null) {
                addToOperationMap(document, "unset", (element._1, ""))
              }
              else {
                addToOperationMap(document, "set", element)
              }
            }

          })

          val result = dao.updateOne(Map[String, Any]("_id" -> new ObjectId(parameter._1)), documentFromScalaMap(document.toMap)).result()
          val insertedResult = UpdateResponse(
            result.wasAcknowledged(),
            Option(result.getUpsertedId).map(value => value.asObjectId().getValue.toHexString).toList,
            result.getModifiedCount,
            result.getMatchedCount
          )
          insertedResult
        }
      )
    )
  }

  private def addToOperationMap(document: mutable.Map[String, Any], operationType: String, element: (String, Any)) = {
    val setMap = document.getOrElse(
      "$" + operationType, {
        val map = mutable.Map[String, Any]()
        document.put("$" + operationType, map)
        map
      }
    )
    val map: mutable.Map[String, Any] = setMap match {
      case value: mutable.Map[String, Any] =>
        value
      case map: Map[String, Any] =>
        val mutableMap = mutable.Map[String, Any]()
        mutableMap ++ map
        mutableMap
    }
    map.put(element._1, element._2)
  }
  override def endpoints: List[ServerEndpoint[AkkaStreams with capabilities.WebSockets, Future]] =
    List(findAllEndpoint) ++
      DocumentManyRoutes.listOfManyEndpoints() ++
      List(insertEndpoint, getDocumentEndpoint, updateSingleDocumentEndpoint, updateDocumentFieldsEndpoint, deleteDocumentEndpoint)

}
