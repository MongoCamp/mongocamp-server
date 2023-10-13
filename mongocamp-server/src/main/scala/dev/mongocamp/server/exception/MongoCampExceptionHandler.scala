package dev.mongocamp.server.exception

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.exception.ErrorDefinition.errorEndpointDefinition
import sttp.model.StatusCode
import sttp.monad.MonadError
import sttp.tapir.server.interceptor.exception.{ ExceptionContext, ExceptionHandler }
import sttp.tapir.server.model.ValuedEndpointOutput

import scala.concurrent.Future

class MongoCampExceptionHandler extends ExceptionHandler[Future] with LazyLogging {

  override def apply(ctx: ExceptionContext)(implicit monad: MonadError[Future]): Future[Option[ValuedEndpointOutput[_]]] = {
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
    monad.unit(Some(response))
  }
}
