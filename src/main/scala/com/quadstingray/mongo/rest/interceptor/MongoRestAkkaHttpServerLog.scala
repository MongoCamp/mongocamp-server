package com.quadstingray.mongo.rest.interceptor

import akka.event.LoggingAdapter
import akka.http.scaladsl.server.RequestContext
import com.quadstingray.mongo.rest.exception.MongoRestException
import sttp.monad.{ FutureMonad, MonadError }
import sttp.tapir.AnyEndpoint
import sttp.tapir.model.{ ServerRequest, ServerResponse }
import sttp.tapir.server.interceptor.log.{ DefaultServerLog, ServerLog }
import sttp.tapir.server.interceptor.{ DecodeFailureContext, DecodeSuccessContext, SecurityFailureContext }

import scala.concurrent.Future

class MongoRestAkkaHttpServerLog extends ServerLog[Future] {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val monadError: MonadError[Future] = new FutureMonad()

  private val defaultServerLog: LoggingAdapter => DefaultServerLog[Future] = (log: LoggingAdapter) => {
    DefaultServerLog[Future](
      doLogWhenHandled = debugLog(log),
      doLogAllDecodeFailures = debugLog(log),
      doLogExceptions = errorLog(log)
    )
  }

  private def loggerFrom(request: ServerRequest): LoggingAdapter = request.underlying.asInstanceOf[RequestContext].log

  private def loggerFrom(ctx: DecodeFailureContext): LoggingAdapter = loggerFrom(ctx.request)

  private def debugLog(log: LoggingAdapter)(msg: String, exOpt: Option[Throwable]): Future[Unit] = Future.successful {
    exOpt match {
      case None     => log.debug(msg)
      case Some(ex) => log.debug(s"$msg; exception: {}", ex)
    }
  }

  private def errorLog(log: LoggingAdapter)(msg: String, ex: Throwable): Future[Unit] = Future.successful {
    if (!ex.isInstanceOf[MongoRestException]) {
      log.error(ex, msg)
    }
  }

  override def decodeFailureNotHandled(ctx: DecodeFailureContext): Future[Unit] =
    defaultServerLog(loggerFrom(ctx)).decodeFailureNotHandled(ctx)

  override def decodeFailureHandled(ctx: DecodeFailureContext, response: ServerResponse[_]): Future[Unit] =
    defaultServerLog(loggerFrom(ctx)).decodeFailureHandled(ctx, response)

  override def securityFailureHandled(ctx: SecurityFailureContext[Future, _], response: ServerResponse[_]): Future[Unit] =
    defaultServerLog(loggerFrom(ctx.request)).securityFailureHandled(ctx, response)

  override def requestHandled(ctx: DecodeSuccessContext[Future, _, _], response: ServerResponse[_]): Future[Unit] =
    defaultServerLog(loggerFrom(ctx.request)).requestHandled(ctx, response)

  override def exception(e: AnyEndpoint, request: ServerRequest, ex: Throwable): Future[Unit] =
    defaultServerLog(loggerFrom(request)).exception(e, request, ex)

}
