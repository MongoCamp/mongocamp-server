package dev.mongocamp.server

import akka.http.scaladsl.server.RequestContext
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.exception.MongoCampExceptionHandler
import dev.mongocamp.server.interceptor.RequestFunctions.requestIdAttributeKey
import dev.mongocamp.server.interceptor._
import dev.mongocamp.server.interceptor.cors.CorsInterceptor
import sttp.tapir.server.akkahttp.{AkkaHttpServerInterpreter, AkkaHttpServerOptions}
import sttp.tapir.server.interceptor.RequestInterceptor
import sttp.tapir.server.interceptor.metrics.MetricsRequestInterceptor

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object AkkaHttpServer extends LazyLogging {
  implicit val ex: ExecutionContext = ActorHandler.requestExecutionContext

  private val serverOptions: AkkaHttpServerOptions = {
    var serverOptions = AkkaHttpServerOptions.customiseInterceptors
      .prependInterceptor(RequestInterceptor.transformServerRequest { request =>
        val requestContext = request.underlying.asInstanceOf[RequestContext]
        val changedContext = requestContext.withRequest(requestContext.request.addAttribute(requestIdAttributeKey, Random.alphanumeric.take(10).mkString))
        Future.successful(request.withUnderlying(changedContext))
      })
      .exceptionHandler(new MongoCampExceptionHandler())
      .decodeFailureHandler(MongoCampDefaultDecodeFailureHandler.handler)
      .serverLog(MongoCampAkkaHttpServerLog.serverLog())
      .addInterceptor(new CorsInterceptor())
      .addInterceptor(new HeadersInterceptor())

    serverOptions = serverOptions.metricsInterceptor(new MetricsRequestInterceptor[Future](List(RequestLogging.responsesDuration()), List()))

    serverOptions.options
  }

  val akkaHttpServerInterpreter: AkkaHttpServerInterpreter = AkkaHttpServerInterpreter(serverOptions)

}
