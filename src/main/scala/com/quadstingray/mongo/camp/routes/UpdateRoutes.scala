package com.quadstingray.mongo.camp.routes

import com.quadstingray.mongo.camp.database.MongoDatabase
import com.quadstingray.mongo.camp.exception.{ ErrorCodes, ErrorDescription, MongoCampException }
import com.quadstingray.mongo.camp.model.auth.AuthorizedCollectionRequest
import com.quadstingray.mongo.camp.model.{ ReplaceOrUpdateRequest, ReplaceResponse, UpdateResponse }
import com.sfxcode.nosql.mongo._
import io.circe.generic.auto._
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object UpdateRoutes extends BaseRoute {

  val replaceEndpoint = writeCollectionEndpoint
    .in("replace")
    .in(jsonBody[ReplaceOrUpdateRequest])
    .out(jsonBody[ReplaceResponse])
    .summary("ReplaceOne in Collection")
    .description("Replace one Document in Collection")
    .tag("Update")
    .method(Method.PUT)
    .name("replace")
    .serverLogic(collectionRequest => parameter => replaceInCollection(collectionRequest, parameter))

  def replaceInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: ReplaceOrUpdateRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), ReplaceResponse]] = {
    Future.successful(
      Right(
        {
          val dao         = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val documentMap = parameter.document
          if (parameter.filter.isEmpty && !documentMap.contains("_id")) {
            throw MongoCampException("no field _id for replace request found", StatusCode.BadRequest, ErrorCodes.idMissingForReplace)
          }
          val result = {
            if (parameter.filter.isEmpty) {
              dao.replaceOne(documentFromScalaMap(documentMap)).result()
            }
            else {
              dao.replaceOne(parameter.filter, documentFromScalaMap(documentMap)).result()
            }
          }
          val insertedResult = ReplaceResponse(
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

  val updateEndpoint = writeCollectionEndpoint
    .in("update")
    .in(jsonBody[ReplaceOrUpdateRequest])
    .out(jsonBody[UpdateResponse])
    .summary("Update One in Collection")
    .description("Update one Document in Collection")
    .tag("Update")
    .method(Method.PUT)
    .name("update")
    .serverLogic(collectionRequest => parameter => updateInCollection(collectionRequest, parameter))

  def updateInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: ReplaceOrUpdateRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UpdateResponse]] = {
    Future.successful(
      Right(
        {
          val dao         = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val documentMap = parameter.document
          if (parameter.filter.isEmpty && !documentMap.contains("_id")) {
            throw MongoCampException("no field _id for replace request found", StatusCode.BadRequest, ErrorCodes.idMissingForReplace)
          }
          val result =
            dao.updateOne(parameter.filter, documentFromScalaMap(documentMap)).result()
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

  val updateManyEndpoint = writeCollectionEndpoint
    .in("update")
    .in("many")
    .in(jsonBody[ReplaceOrUpdateRequest])
    .out(jsonBody[UpdateResponse])
    .summary("Update many in Collection")
    .description("Update many Document in Collection")
    .tag("Update")
    .method(Method.PUT)
    .name("update")
    .serverLogic(collectionRequest => parameter => updateManyInCollection(collectionRequest, parameter))

  def updateManyInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: ReplaceOrUpdateRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UpdateResponse]] = {
    Future.successful(
      Right(
        {
          val dao         = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
          val documentMap = parameter.document
          if (parameter.filter.isEmpty && !documentMap.contains("_id")) {
            throw MongoCampException("no field _id for replace request found", StatusCode.BadRequest, ErrorCodes.idMissingForReplace)
          }
          val result =
            dao.updateOne(parameter.filter, documentFromScalaMap(documentMap)).result()
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

  lazy val updateEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = List(replaceEndpoint, updateEndpoint, updateManyEndpoint)

}
