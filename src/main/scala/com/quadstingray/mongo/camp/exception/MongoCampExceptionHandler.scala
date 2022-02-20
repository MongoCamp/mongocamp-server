package com.quadstingray.mongo.camp.exception

import com.quadstingray.mongo.camp.exception.ErrorDefinition.errorEndpointDefinition
import com.typesafe.scalalogging.LazyLogging
import sttp.model.StatusCode
import sttp.tapir.server.interceptor.ValuedEndpointOutput
import sttp.tapir.server.interceptor.exception.{ ExceptionContext, ExceptionHandler }

class MongoCampExceptionHandler extends ExceptionHandler with LazyLogging {

  override def apply(ctx: ExceptionContext): Option[ValuedEndpointOutput[_]] = {
    val internalErrorStatusCode = StatusCode.InternalServerError
    val response = ctx.e match {
      case jEx: MongoCampException =>
        val description = ErrorDescription(jEx.errorCode, jEx.message, jEx.additionalInfo)
        ValuedEndpointOutput(errorEndpointDefinition, (jEx.statusCode, description, description))
      case ex: Exception =>
        logger.error(ex.getMessage, ex)
        val description = ErrorDescription(-1, ex.getMessage)
        ValuedEndpointOutput(errorEndpointDefinition, (internalErrorStatusCode, description, description))
      case any: Any =>
        logger.error("None catchable Error <%s>".format(any.toString))
        val description = ErrorDescription(-1, "Unknown Error")
        ValuedEndpointOutput(errorEndpointDefinition, (internalErrorStatusCode, description, description))
    }
    Some(response)
  }

}
