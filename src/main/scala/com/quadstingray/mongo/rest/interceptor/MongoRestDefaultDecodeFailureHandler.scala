package com.quadstingray.mongo.rest.interceptor

import com.quadstingray.mongo.rest.exception.ErrorDefinition.{ HeaderErrorAdditionalInfo, HeaderErrorCode, HeaderErrorMessage }
import com.quadstingray.mongo.rest.exception.ErrorDescription
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import sttp.model.{ Header, StatusCode }
import sttp.tapir.server.interceptor.decodefailure.DecodeFailureHandler
import sttp.tapir.server.interceptor.decodefailure.DefaultDecodeFailureHandler.{ failureResponse, respond, FailureMessages }
import sttp.tapir.server.interceptor.{ DecodeFailureContext, ValuedEndpointOutput }

case class MongoRestDefaultDecodeFailureHandler(
    respond: DecodeFailureContext => Option[(StatusCode, List[Header])],
    failureMessage: DecodeFailureContext => String,
    response: (StatusCode, List[Header], String) => ValuedEndpointOutput[_]
) extends DecodeFailureHandler {
  def apply(ctx: DecodeFailureContext): Option[ValuedEndpointOutput[_]] = {
    respond(ctx) match {
      case Some((sc, hs)) =>
        val failureMsg         = failureMessage(ctx)
        val mongoRestException = ErrorDescription(4001, failureMsg)
        Some(
          response(
            sc,
            hs ++ List(
              Header(HeaderErrorMessage, mongoRestException.msg),
              Header(HeaderErrorCode, mongoRestException.code.toString),
              Header(HeaderErrorAdditionalInfo, mongoRestException.additionalInfo),
              Header("Content-Type", "application/json")
            ),
            mongoRestException.asJson.toString()
          )
        )
      case None => None
    }
  }
}

object MongoRestDefaultDecodeFailureHandler {
  def handler: MongoRestDefaultDecodeFailureHandler = MongoRestDefaultDecodeFailureHandler(
    respond(_, badRequestOnPathErrorIfPathShapeMatches = false, badRequestOnPathInvalidIfPathShapeMatches = true),
    FailureMessages.failureMessage,
    failureResponse
  )
}
