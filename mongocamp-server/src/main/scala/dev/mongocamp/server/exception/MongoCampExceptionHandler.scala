package dev.mongocamp.server.exception

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.exception.ErrorDefinition.errorEndpointDefinition
import scala.concurrent.Future
import sttp.model.StatusCode
import sttp.monad.MonadError
import sttp.tapir.server.interceptor.exception.ExceptionContext
import sttp.tapir.server.interceptor.exception.ExceptionHandler
import sttp.tapir.server.model.ValuedEndpointOutput

class MongoCampExceptionHandler extends ExceptionHandler[Future] with LazyLogging {

  override def apply(ctx: ExceptionContext)(implicit monad: MonadError[Future]): Future[Option[ValuedEndpointOutput[_]]] = {
    val internalErrorStatusCode = StatusCode.InternalServerError
    val response = ctx.e match {
      case jEx: MongoCampException =>
        val description = ErrorDescription(jEx.errorCode, jEx.message, jEx.additionalInfo)
        ValuedEndpointOutput(errorEndpointDefinition, (jEx.statusCode, description, description))
      case ex: Exception =>
        // the full error is logged, the client only gets a description without the address of the database server
        val databaseException = DatabaseErrorDescription.findDatabaseException(ex)
        val description       = databaseException.map(DatabaseErrorDescription.describe).getOrElse(ErrorDescription(-1, ex.getMessage))
        val statusCode        = databaseException.map(DatabaseErrorDescription.statusCode).getOrElse(internalErrorStatusCode)
        if (statusCode.isServerError) logger.error(ex.getMessage, ex) else logger.warn(ex.getMessage)
        ValuedEndpointOutput(errorEndpointDefinition, (statusCode, description, description))
      case any: Any =>
        logger.error("None catchable Error <%s>".format(any.toString))
        val description = ErrorDescription(-1, "Unknown Error")
        ValuedEndpointOutput(errorEndpointDefinition, (internalErrorStatusCode, description, description))
    }
    monad.unit(Some(response))
  }
}
