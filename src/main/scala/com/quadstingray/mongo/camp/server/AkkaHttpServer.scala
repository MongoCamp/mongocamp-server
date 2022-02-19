package com.quadstingray.mongo.camp.server

import com.quadstingray.mongo.camp.config.Config
import com.quadstingray.mongo.camp.interceptor._
import com.quadstingray.mongo.camp.interceptor.cors.CorsInterceptor
import com.typesafe.scalalogging.LazyLogging
import sttp.tapir.server.akkahttp.{ AkkaHttpServerInterpreter, AkkaHttpServerOptions }

object AkkaHttpServer extends LazyLogging with Config {

  private val serverOptions: AkkaHttpServerOptions = {
    AkkaHttpServerOptions.customInterceptors
      .exceptionHandler(new MongoCampExceptionHandler())
      .decodeFailureHandler(MongoCampDefaultDecodeFailureHandler.handler)
      .addInterceptor(new CorsInterceptor())
      .addInterceptor(new HeadersInterceptor())
      .serverLog(new MongoCampAkkaHttpServerLog())
      .options
  }

  val akkaHttpServerInterpreter: AkkaHttpServerInterpreter = AkkaHttpServerInterpreter(serverOptions)

}
