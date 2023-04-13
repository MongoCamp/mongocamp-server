package dev.mongocamp.server

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.exception.MongoCampExceptionHandler
import dev.mongocamp.server.interceptor._
import dev.mongocamp.server.interceptor.cors.CorsInterceptor
import sttp.tapir.server.akkahttp.{ AkkaHttpServerInterpreter, AkkaHttpServerOptions }
import sttp.tapir.server.interceptor.metrics.MetricsRequestInterceptor

import scala.concurrent.{ ExecutionContext, Future }

object AkkaHttpServer extends LazyLogging {
  implicit val ex: ExecutionContext = ActorHandler.requestExecutionContext

  private val serverOptions: AkkaHttpServerOptions = {
    var serverOptions = AkkaHttpServerOptions.customiseInterceptors
      .exceptionHandler(new MongoCampExceptionHandler())
      .decodeFailureHandler(MongoCampDefaultDecodeFailureHandler.handler)
      .addInterceptor(new CorsInterceptor())
      .addInterceptor(new HeadersInterceptor())
      .serverLog(MongoCampAkkaHttpServerLog.serverLog())

    serverOptions = serverOptions.metricsInterceptor(new MetricsRequestInterceptor[Future](List(RequestLogging.responsesDuration()), List()))

    serverOptions.options
  }

  val akkaHttpServerInterpreter: AkkaHttpServerInterpreter = AkkaHttpServerInterpreter(serverOptions)

}
