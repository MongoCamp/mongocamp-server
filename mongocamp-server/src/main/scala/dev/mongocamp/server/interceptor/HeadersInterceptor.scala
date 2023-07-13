package dev.mongocamp.server.interceptor

import dev.mongocamp.server.interceptor.RequestFunctions.mongoCampRequestIdKey
import dev.mongocamp.server.{ActorHandler, BuildInfo}
import sttp.model.Header
import sttp.monad.MonadError
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.interceptor._
import sttp.tapir.server.interpreter.BodyListener
import sttp.tapir.server.model.ServerResponse

import scala.concurrent.{ExecutionContext, Future}

class HeadersInterceptor extends EndpointInterceptor[Future] {

  implicit val ex: ExecutionContext = ActorHandler.requestExecutionContext

  private def addHeaders(request: ServerRequest): List[Header] = {
    List(Header("server", s"${BuildInfo.name}/${BuildInfo.version}")) ++ RequestFunctions
      .getRequestIdOption(request)
      .map(requestId => Header(mongoCampRequestIdKey, s"${requestId}"))
  }

  override def apply[B](responder: Responder[Future, B], endpointHandler: EndpointHandler[Future, B]): EndpointHandler[Future, B] = {
    new EndpointHandler[Future, B] {

      override def onDecodeSuccess[A, U, I](
          ctx: DecodeSuccessContext[Future, A, U, I]
      )(implicit monad: MonadError[Future], bodyListener: BodyListener[Future, B]): Future[ServerResponse[B]] = {
        endpointHandler
          .onDecodeSuccess(ctx)
          .map(serverResponse => serverResponse.copy(headers = serverResponse.headers ++ addHeaders(ctx.request)))
      }

      override def onSecurityFailure[A](
          ctx: SecurityFailureContext[Future, A]
      )(implicit monad: MonadError[Future], bodyListener: BodyListener[Future, B]): Future[ServerResponse[B]] = {
        endpointHandler
          .onSecurityFailure(ctx)
          .map(serverResponse => serverResponse.copy(headers = serverResponse.headers ++ addHeaders(ctx.request)))
      }

      override def onDecodeFailure(
          ctx: DecodeFailureContext
      )(implicit monad: MonadError[Future], bodyListener: BodyListener[Future, B]): Future[Option[ServerResponse[B]]] = {
        endpointHandler
          .onDecodeFailure(ctx)
          .map(serverResponse => serverResponse.map(response => response.copy(headers = response.headers ++ addHeaders(ctx.request))))
      }
    }
  }

}
