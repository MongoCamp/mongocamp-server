package com.quadstingray.mongo.camp.server

import com.quadstingray.mongo.camp.config.Config
import com.quadstingray.mongo.camp.interceptor._
import com.quadstingray.mongo.camp.interceptor.cors.CorsInterceptor
import com.typesafe.scalalogging.LazyLogging
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

    if (globalConfigBoolean("requestlogging.enabled")) {
      serverOptions = serverOptions.metricsInterceptor(new MetricsRequestInterceptor[Future](List(RequestLogging.responsesDuration()), List()))
    }

    serverOptions.options
  }

  val akkaHttpServerInterpreter: AkkaHttpServerInterpreter = AkkaHttpServerInterpreter(serverOptions)

}
