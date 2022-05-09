package dev.mongocamp.server.interceptor

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.server.RequestContext
import dev.mongocamp.server.ActorHandler
import dev.mongocamp.server.exception.MongoCampException
import sttp.monad.{ FutureMonad, MonadError }
import sttp.tapir.AnyEndpoint
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.interceptor.log.{ DefaultServerLog, ServerLog }
import sttp.tapir.server.interceptor.{ DecodeFailureContext, DecodeSuccessContext, SecurityFailureContext, ServerResponseFromOutput }

import scala.concurrent.{ ExecutionContext, Future }

class MongoCampAkkaHttpServerLog extends ServerLog[Future] {
  implicit val ex: ExecutionContext = ActorHandler.requestExecutionContext

  implicit val monadError: MonadError[Future] = new FutureMonad()

  private val defaultServerLog: LoggingAdapter => DefaultServerLog[Future] = (log: LoggingAdapter) => {
    DefaultServerLog[Future](
      doLogWhenHandled = debugLog(log),
      doLogAllDecodeFailures = debugLog(log),
      doLogExceptions = errorLog(log),
      noLog = Future.successful(())
    )
  }

  private def loggerFrom(request: ServerRequest): LoggingAdapter = request.underlying match {
    case rc: RequestContext => rc.log
    case _                  => NoLogging
  }
  private def loggerFrom(ctx: DecodeFailureContext): LoggingAdapter = loggerFrom(ctx.request)

  private def debugLog(log: LoggingAdapter)(msg: String, exOpt: Option[Throwable]): Future[Unit] = Future.successful {
    exOpt match {
      case None     => log.debug(msg)
      case Some(ex) => log.debug(s"$msg; exception: {}", ex)
    }
  }

  private def errorLog(log: LoggingAdapter)(msg: String, ex: Throwable): Future[Unit] = Future.successful {
    if (!ex.isInstanceOf[MongoCampException]) {
      log.error(ex, msg)
    }
  }

  override def decodeFailureNotHandled(ctx: DecodeFailureContext): Future[Unit] = {
    defaultServerLog(loggerFrom(ctx)).decodeFailureNotHandled(ctx)
  }

  override def decodeFailureHandled(ctx: DecodeFailureContext, response: ServerResponseFromOutput[_]): Future[Unit] = {
    defaultServerLog(loggerFrom(ctx)).decodeFailureHandled(ctx, response)
  }

  override def securityFailureHandled(ctx: SecurityFailureContext[Future, _], response: ServerResponseFromOutput[_]): Future[Unit] = {
    defaultServerLog(loggerFrom(ctx.request)).securityFailureHandled(ctx, response)
  }

  override def requestHandled(ctx: DecodeSuccessContext[Future, _, _], response: ServerResponseFromOutput[_]): Future[Unit] = {
    defaultServerLog(loggerFrom(ctx.request)).requestHandled(ctx, response)
  }

  override def exception(e: AnyEndpoint, request: ServerRequest, ex: Throwable): Future[Unit] = {
    defaultServerLog(loggerFrom(request)).exception(e, request, ex)
  }

}
