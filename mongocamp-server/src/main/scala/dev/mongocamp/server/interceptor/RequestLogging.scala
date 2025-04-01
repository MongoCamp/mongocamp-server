package dev.mongocamp.server.interceptor

import dev.mongocamp.server.auth.{ AuthHolder, TokenCache }
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.http.{ HttpRequestCompletedEvent, HttpRequestStartEvent }
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.interceptor.RequestFunctions.{ requestHeaderKeyRealIp, requestHeaderKeyRemoteAddress }
import org.joda.time.DateTime
import sttp.model.HeaderNames
import sttp.tapir.server.metrics.{ EndpointMetric, Metric, MetricLabels }

object RequestLogging {
  def responsesDuration[F[_]](labels: MetricLabels = MetricLabels.Default): Metric[F, _] =
    Metric[F, Unit](
      RequestLogging,
      onRequest = {
        (request, histogram, m) =>
          m.unit {
            val requestId    = RequestFunctions.getRequestIdOption(request).getOrElse(request.hashCode().toString)
            val requestStart = new DateTime()
            val remoteAddress: Option[String] = request
              .header(HeaderNames.XForwardedFor)
              .flatMap(_.split(",").headOption)
              .orElse(request.header(requestHeaderKeyRemoteAddress))
              .orElse(request.header(requestHeaderKeyRealIp))
              .orElse(
                request.connectionInfo.remote.flatMap(
                  a => Option(a.getAddress.getHostAddress)
                )
              )

            val user: String = request
              .header(HeaderNames.Authorization)
              .map(
                auth => {
                  if (auth.startsWith("Bearer ")) {
                    val token = auth.split(" ").last
                    TokenCache.validateToken(token).map(_.userId).getOrElse("INVALID_TOKEN")
                  }
                  else {
                    "INVALID_AUTH"
                  }
                }
              )
              .getOrElse(
                request
                  .header("X-AUTH-APIKEY")
                  .map(
                    key => AuthHolder.handler.findUserByApiKeyOption(key).map(_.userId).getOrElse("INVALID_APIKEY")
                  )
                  .getOrElse("")
              )

            EndpointMetric()
              .onResponseBody {
                (endpoint, response) =>
                  m.eval({
                    val headOfTags     = endpoint.info.tags.headOption.getOrElse("NOT_SET")
                    val controllerName = headOfTags.replace("/", "").split(' ').map(_.capitalize).mkString("")
                    val methodeName    = endpoint.info.name.getOrElse(headOfTags)
                    val requestCompletedEvent = HttpRequestCompletedEvent(
                      requestId,
                      request.method.method,
                      methodeName,
                      request.uri.toString(),
                      remoteAddress.getOrElse("NOT_SET"),
                      user,
                      new org.joda.time.Duration(requestStart, new DateTime()),
                      response.code.code,
                      controllerName,
                      methodeName,
                      endpoint.info.description.getOrElse("NOT_SET")
                    )
                    EventSystem.publish(requestCompletedEvent)
                    histogram
                  })
              }
              .onException {
                (endpoint, exception) =>
                  m.eval({

                    val statusCode: Int = exception match {
                      case e: MongoCampException =>
                        e.statusCode.code
                      case _ =>
                        500
                    }

                    val headOfTags     = endpoint.info.tags.headOption.getOrElse("NOT_SET")
                    val controllerName = headOfTags.replace("/", "").split(' ').map(_.capitalize).mkString("")
                    val methodeName    = endpoint.info.name.getOrElse(headOfTags)
                    val requestCompletedEvent = HttpRequestCompletedEvent(
                      requestId,
                      request.method.method,
                      methodeName,
                      request.uri.toString(),
                      remoteAddress.getOrElse("NOT_SET"),
                      user,
                      new org.joda.time.Duration(requestStart, new DateTime()),
                      statusCode,
                      controllerName,
                      methodeName,
                      endpoint.info.description.getOrElse("NOT_SET")
                    )
                    EventSystem.publish(requestCompletedEvent)
                    histogram
                  })
              }
              .onEndpointRequest(
                (endpoint) =>
                  m.eval({
                    val headOfTags     = endpoint.info.tags.headOption.getOrElse("NOT_SET")
                    val controllerName = headOfTags.replace("/", "").split(' ').map(_.capitalize).mkString("")
                    val methodeName    = endpoint.info.name.getOrElse(headOfTags)

                    val requestStartEvent =
                      HttpRequestStartEvent(
                        requestId,
                        request.method.method,
                        methodeName,
                        request.uri.toString(),
                        remoteAddress.getOrElse("NOT_SET"),
                        user,
                        controllerName,
                        methodeName,
                        endpoint.info.description.getOrElse("NOT_SET")
                      )

                    EventSystem.publish(requestStartEvent)
                    endpoint
                  })
              )

          }
      }
    )

}
