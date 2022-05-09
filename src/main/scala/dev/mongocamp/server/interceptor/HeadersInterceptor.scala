package dev.mongocamp.server.interceptor

import dev.mongocamp.server.config.Config
import dev.mongocamp.server.{ ActorHandler, BuildInfo }
import sttp.model.Header
import sttp.monad.MonadError
import sttp.tapir.server.interceptor._
import sttp.tapir.server.interpreter.BodyListener

import scala.concurrent.{ ExecutionContext, Future }

class HeadersInterceptor extends EndpointInterceptor[Future] with Config {

  implicit val ex: ExecutionContext = ActorHandler.requestExecutionContext

  private def addHeaders(): List[Header] = {
    List(
      Header("server", s"${BuildInfo.name}/${BuildInfo.version}")
    )
  }

  override def apply[B](responder: Responder[Future, B], endpointHandler: EndpointHandler[Future, B]): EndpointHandler[Future, B] = {
    new EndpointHandler[Future, B] {

      override def onDecodeSuccess[U, I](
          ctx: DecodeSuccessContext[Future, U, I]
      )(implicit monad: MonadError[Future], bodyListener: BodyListener[Future, B]): Future[ServerResponseFromOutput[B]] = {
        endpointHandler
          .onDecodeSuccess(ctx)
          .map(serverResponse => serverResponse.copy(headers = serverResponse.headers ++ addHeaders()))
      }

      override def onSecurityFailure[A](
          ctx: SecurityFailureContext[Future, A]
      )(implicit monad: MonadError[Future], bodyListener: BodyListener[Future, B]): Future[ServerResponseFromOutput[B]] = {
        endpointHandler
          .onSecurityFailure(ctx)
          .map(serverResponse => serverResponse.copy(headers = serverResponse.headers ++ addHeaders()))
      }

      override def onDecodeFailure(
          ctx: DecodeFailureContext
      )(implicit monad: MonadError[Future], bodyListener: BodyListener[Future, B]): Future[Option[ServerResponseFromOutput[B]]] = {
        endpointHandler
          .onDecodeFailure(ctx)
          .map(serverResponse => serverResponse.map(response => response.copy(headers = response.headers ++ addHeaders())))
      }
    }
  }

}
