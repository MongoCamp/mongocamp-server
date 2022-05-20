package dev.mongocamp.server.interceptor.cors

import dev.mongocamp.server.ActorHandler
import dev.mongocamp.server.interceptor.cors.Cors.KeyCorsHeaderOrigin
import sttp.model.Header
import sttp.monad.MonadError
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.interceptor._
import sttp.tapir.server.interpreter.BodyListener

import scala.concurrent.{ ExecutionContext, Future }

class CorsInterceptor extends EndpointInterceptor[Future] {
  implicit val ex: ExecutionContext = ActorHandler.requestExecutionContext

  private def corsHeaderFromRequest(request: ServerRequest): List[Header] = {
    val origin  = request.header(KeyCorsHeaderOrigin)
    val referer = request.header(KeyCorsHeaderOrigin).map(string => if (string.endsWith("/")) string.replaceAll("/$", "") else string)
    Cors.corsHeadersFromOrigin((origin ++ referer).headOption)
  }

  override def apply[B](responder: Responder[Future, B], endpointHandler: EndpointHandler[Future, B]): EndpointHandler[Future, B] = {
    new EndpointHandler[Future, B] {

      override def onDecodeSuccess[U, I](
          ctx: DecodeSuccessContext[Future, U, I]
      )(implicit monad: MonadError[Future], bodyListener: BodyListener[Future, B]): Future[ServerResponseFromOutput[B]] = {
        endpointHandler
          .onDecodeSuccess(ctx)
          .map(serverResponse => serverResponse.copy(headers = serverResponse.headers ++ corsHeaderFromRequest(ctx.request)))
      }

      override def onSecurityFailure[A](
          ctx: SecurityFailureContext[Future, A]
      )(implicit monad: MonadError[Future], bodyListener: BodyListener[Future, B]): Future[ServerResponseFromOutput[B]] = {
        endpointHandler
          .onSecurityFailure(ctx)
          .map(serverResponse => serverResponse.copy(headers = serverResponse.headers ++ corsHeaderFromRequest(ctx.request)))
      }

      override def onDecodeFailure(
          ctx: DecodeFailureContext
      )(implicit monad: MonadError[Future], bodyListener: BodyListener[Future, B]): Future[Option[ServerResponseFromOutput[B]]] = {
        endpointHandler
          .onDecodeFailure(ctx)
          .map(serverResponse => serverResponse.map(response => response.copy(headers = response.headers ++ corsHeaderFromRequest(ctx.request))))
      }

    }

  }
}
