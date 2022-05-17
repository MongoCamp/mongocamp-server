package dev.mongocamp.server

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.config.Config
import dev.mongocamp.server.interceptor._
import dev.mongocamp.server.interceptor.cors.CorsInterceptor
import sttp.tapir.server.akkahttp.{ AkkaHttpServerInterpreter, AkkaHttpServerOptions }
import sttp.tapir.server.interceptor.metrics.MetricsRequestInterceptor

import scala.concurrent.Future

object AkkaHttpServer extends LazyLogging with Config {

  private val serverOptions: AkkaHttpServerOptions = {
    var serverOptions = AkkaHttpServerOptions.customInterceptors
      .exceptionHandler(new MongoCampExceptionHandler())
      .decodeFailureHandler(MongoCampDefaultDecodeFailureHandler.handler)
      .addInterceptor(new CorsInterceptor())
      .addInterceptor(new HeadersInterceptor())
      .serverLog(new MongoCampAkkaHttpServerLog())

    serverOptions = serverOptions.metricsInterceptor(new MetricsRequestInterceptor[Future](List(RequestLogging.responsesDuration()), List()))

    serverOptions.options
  }

  val akkaHttpServerInterpreter: AkkaHttpServerInterpreter = AkkaHttpServerInterpreter(serverOptions)

}
