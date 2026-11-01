package dev.mongocamp.server

import org.apache.pekko.http.scaladsl.server.RequestContext
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.exception.MongoCampExceptionHandler
import dev.mongocamp.server.interceptor.RequestFunctions.requestIdAttributeKey
import dev.mongocamp.server.interceptor._
import dev.mongocamp.server.interceptor.cors.CorsInterceptor
import sttp.tapir.server.pekkohttp.{ PekkoHttpServerInterpreter, PekkoHttpServerOptions }
import sttp.tapir.server.interceptor.exception.ExceptionInterceptor
import sttp.tapir.server.interceptor.RequestInterceptor
import sttp.tapir.server.interceptor.metrics.MetricsRequestInterceptor

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random

object HttpServer extends LazyLogging {
  implicit val ex: ExecutionContext = ActorHandler.requestExecutionContext

  private val serverOptions: PekkoHttpServerOptions = {
    var serverOptions = PekkoHttpServerOptions.customiseInterceptors
      .prependInterceptor(RequestInterceptor.transformServerRequest {
        request =>
          val requestContext = request.underlying.asInstanceOf[RequestContext]
          val changedContext = requestContext.withRequest(requestContext.request.addAttribute(requestIdAttributeKey, Random.alphanumeric.take(10).mkString))
          Future.successful(request.withUnderlying(changedContext))
      })
      // the exception handler is added as the last (innermost) interceptor, so that its responses also get the headers of the other interceptors (cors, server, request id)
      .exceptionHandler(None)
      .decodeFailureHandler(MongoCampDefaultDecodeFailureHandler.handler)
      .serverLog(MongoCampHttpServerLog.serverLog())
      .addInterceptor(new CorsInterceptor())
      .addInterceptor(new HeadersInterceptor())
      .addInterceptor(new ExceptionInterceptor[Future](new MongoCampExceptionHandler()))

    serverOptions = serverOptions.metricsInterceptor(new MetricsRequestInterceptor[Future](List(RequestLogging.responsesDuration()), List()))

    serverOptions.options
  }

  val httpServerInterpreter: PekkoHttpServerInterpreter = PekkoHttpServerInterpreter(serverOptions)

}
