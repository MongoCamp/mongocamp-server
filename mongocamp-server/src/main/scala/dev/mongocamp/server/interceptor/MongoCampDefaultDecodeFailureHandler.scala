package dev.mongocamp.server.interceptor

import dev.mongocamp.server.exception.ErrorDefinition.{ HeaderErrorAdditionalInfo, HeaderErrorCode, HeaderErrorMessage }
import dev.mongocamp.server.exception.ErrorDescription
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import sttp.model.{ Header, StatusCode }
import sttp.tapir.server.interceptor.DecodeFailureContext
import sttp.tapir.server.interceptor.decodefailure.DecodeFailureHandler
import sttp.tapir.server.interceptor.decodefailure.DefaultDecodeFailureHandler.{ failureResponse, respond, FailureMessages }
import sttp.tapir.server.model.ValuedEndpointOutput

case class MongoCampDefaultDecodeFailureHandler(
    respond: DecodeFailureContext => Option[(StatusCode, List[Header])],
    failureMessage: DecodeFailureContext => String,
    response: (StatusCode, List[Header], String) => ValuedEndpointOutput[_]
) extends DecodeFailureHandler {
  def apply(ctx: DecodeFailureContext): Option[ValuedEndpointOutput[_]] = {
    respond(ctx) match {
      case Some((sc, hs)) =>
        val failureMsg         = failureMessage(ctx)
        val mongoCampException = ErrorDescription(4001, failureMsg)
        Some(
          response(
            sc,
            hs ++ List(
              Header(HeaderErrorMessage, mongoCampException.msg),
              Header(HeaderErrorCode, mongoCampException.code.toString),
              Header(HeaderErrorAdditionalInfo, mongoCampException.additionalInfo),
              Header("Content-Type", "application/json")
            ),
            mongoCampException.asJson.toString()
          )
        )
      case None => None
    }
  }
}

object MongoCampDefaultDecodeFailureHandler {
  def handler: MongoCampDefaultDecodeFailureHandler = MongoCampDefaultDecodeFailureHandler(
    respond,
    FailureMessages.failureMessage,
    failureResponse
  )
}
