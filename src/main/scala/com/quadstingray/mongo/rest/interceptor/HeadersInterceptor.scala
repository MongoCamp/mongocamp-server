package com.quadstingray.mongo.rest.interceptor

import com.quadstingray.mongo.rest.BuildInfo
import com.quadstingray.mongo.rest.config.Config
import sttp.model.Header
import sttp.monad.MonadError
import sttp.tapir.model.ServerResponse
import sttp.tapir.server.interceptor._
import sttp.tapir.server.interpreter.BodyListener

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HeadersInterceptor extends EndpointInterceptor[Future] with Config {

  private def addHeaders(): List[Header] = {
    List(
      Header("server", "%s/%s".format(BuildInfo.name, BuildInfo.version))
    )
  }

  override def apply[B](responder: Responder[Future, B], endpointHandler: EndpointHandler[Future, B]): EndpointHandler[Future, B] = {
    new EndpointHandler[Future, B] {

      override def onDecodeSuccess[U, I](
          ctx: DecodeSuccessContext[Future, U, I]
      )(implicit monad: MonadError[Future], bodyListener: BodyListener[Future, B]): Future[ServerResponse[B]] = {
        endpointHandler
          .onDecodeSuccess(ctx)
          .map(serverResponse => serverResponse.copy(headers = serverResponse.headers ++ addHeaders()))
      }

      override def onSecurityFailure[A](
          ctx: SecurityFailureContext[Future, A]
      )(implicit monad: MonadError[Future], bodyListener: BodyListener[Future, B]): Future[ServerResponse[B]] = {
        endpointHandler
          .onSecurityFailure(ctx)
          .map(serverResponse => serverResponse.copy(headers = serverResponse.headers ++ addHeaders()))
      }

      override def onDecodeFailure(
          ctx: DecodeFailureContext
      )(implicit monad: MonadError[Future], bodyListener: BodyListener[Future, B]): Future[Option[ServerResponse[B]]] = {
        endpointHandler
          .onDecodeFailure(ctx)
          .map(serverResponse => serverResponse.map(response => response.copy(headers = response.headers ++ addHeaders())))
      }
    }
  }

}
