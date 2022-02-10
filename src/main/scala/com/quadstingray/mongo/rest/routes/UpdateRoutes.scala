package com.quadstingray.mongo.rest.routes

import com.quadstingray.mongo.rest.database.MongoDatabase
import com.quadstingray.mongo.rest.exception.{ ErrorCodes, ErrorDescription, MongoRestException }
import com.quadstingray.mongo.rest.model.auth.UserInformation
import com.quadstingray.mongo.rest.model.{ ReplaceOrUpdateRequest, ReplaceResponse, UpdateResponse }
import com.sfxcode.nosql.mongo._
import io.circe.generic.auto._
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object UpdateRoutes extends BaseRoute {

  val replaceEndpoint = collectionEndpoint
    .in("replace")
    .in(jsonBody[ReplaceOrUpdateRequest])
    .out(jsonBody[ReplaceResponse])
    .summary("ReplaceOne in Collection")
    .description("Replace one Document in Collection")
    .tag("Update")
    .method(Method.PUT)
    .name("replace")
    .serverLogic(connection => parameter => replaceInCollection(connection, parameter))

  def replaceInCollection(
      user: UserInformation,
      parameter: (String, ReplaceOrUpdateRequest)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), ReplaceResponse]] = {
    Future.successful(
      Right(
        {
          val dao         = MongoDatabase.databaseProvider.dao(parameter._1)
          val documentMap = parameter._2.document
          if (parameter._2.filter.isEmpty && !documentMap.contains("_id")) {
            throw MongoRestException("no field _id for replace request found", StatusCode.BadRequest, ErrorCodes.idMissingForReplace)
          }
          val result = {
            if (parameter._2.filter.isEmpty) {
              dao.replaceOne(documentFromScalaMap(documentMap)).result()
            }
            else {
              dao.replaceOne(parameter._2.filter, documentFromScalaMap(documentMap)).result()
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

  val updateEndpoint = collectionEndpoint
    .in("update")
    .in(jsonBody[ReplaceOrUpdateRequest])
    .out(jsonBody[UpdateResponse])
    .summary("Update One in Collection")
    .description("Update one Document in Collection")
    .tag("Update")
    .method(Method.PUT)
    .name("update")
    .serverLogic(connection => parameter => updateInCollection(connection, parameter))

  def updateInCollection(
      user: UserInformation,
      parameter: (String, ReplaceOrUpdateRequest)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UpdateResponse]] = {
    Future.successful(
      Right(
        {
          val dao         = MongoDatabase.databaseProvider.dao(parameter._1)
          val documentMap = parameter._2.document
          if (parameter._2.filter.isEmpty && !documentMap.contains("_id")) {
            throw MongoRestException("no field _id for replace request found", StatusCode.BadRequest, ErrorCodes.idMissingForReplace)
          }
          val result =
            dao.updateOne(parameter._2.filter, documentFromScalaMap(documentMap)).result()
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

  val updateManyEndpoint = collectionEndpoint
    .in("update")
    .in("many")
    .in(jsonBody[ReplaceOrUpdateRequest])
    .out(jsonBody[UpdateResponse])
    .summary("Update many in Collection")
    .description("Update many Document in Collection")
    .tag("Update")
    .method(Method.PUT)
    .name("update")
    .serverLogic(connection => parameter => updateManyInCollection(connection, parameter))

  def updateManyInCollection(
      user: UserInformation,
      parameter: (String, ReplaceOrUpdateRequest)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UpdateResponse]] = {
    Future.successful(
      Right(
        {
          val dao         = MongoDatabase.databaseProvider.dao(parameter._1)
          val documentMap = parameter._2.document
          if (parameter._2.filter.isEmpty && !documentMap.contains("_id")) {
            throw MongoRestException("no field _id for replace request found", StatusCode.BadRequest, ErrorCodes.idMissingForReplace)
          }
          val result =
            dao.updateOne(parameter._2.filter, documentFromScalaMap(documentMap)).result()
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
