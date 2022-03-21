package com.quadstingray.mongo.camp.interceptor
import com.quadstingray.mongo.camp.BuildInfo
import com.quadstingray.mongo.camp.auth.{ AuthHolder, TokenCache }
import com.quadstingray.mongo.camp.database.MongoDatabase
import com.quadstingray.mongo.camp.exception.MongoCampException
import com.sfxcode.nosql.mongo._
import org.joda.time.DateTime
import sttp.model.HeaderNames
import sttp.tapir.metrics.{ EndpointMetric, Metric, MetricLabels }

import java.net.InetAddress
import java.util.Date

case class RequestLogging(
    date: Date,
    applicationName: String,
    version: String,
    containerName: String,
    requestId: Int,
    httpMethod: String,
    methodName: String,
    uri: String,
    remoteAddress: String,
    userId: String,
    duration: Long,
    responseCode: Int,
    controller: String,
    controllerMethod: String,
    comment: String
)

object RequestLogging {
  def responsesDuration[F[_]](labels: MetricLabels = MetricLabels.Default): Metric[F, _] =
    Metric[F, Unit](
      RequestLogging,
      onRequest = { (request, histogram, m) =>
        m.unit {
          val requestId    = request.hashCode()
          val requestStart = new DateTime()
          val remoteAddress: Option[String] = request
            .header(HeaderNames.XForwardedFor)
            .flatMap(_.split(",").headOption)
            .orElse(request.header("Remote-Address"))
            .orElse(request.header("X-Real-Ip"))
            .orElse(request.connectionInfo.remote.flatMap(a => Option(a.getAddress.getHostAddress)))

          val user: String = request
            .header(HeaderNames.Authorization)
            .map(auth => {
              if (auth.startsWith("Bearer ")) {
                val token = auth.split(" ").last
                TokenCache.validateToken(token).map(_.userId).getOrElse("INVALID_TOKEN")
              }
              else {
                "INVALID_AUTH"
              }
            })
            .getOrElse(
              request
                .header("X-AUTH-APIKEY")
                .map(key => AuthHolder.handler.findUserByApiKeyOption(key).map(_.userId).getOrElse("INVALID_APIKEY"))
                .getOrElse("")
            )

          EndpointMetric()
            .onResponse { (endpoint, response) =>
              m.eval({
                val headOfTags     = endpoint.info.tags.headOption.getOrElse("NOT_SET")
                val controllerName = headOfTags.replace("/", "").split(' ').map(_.capitalize).mkString("")
                val methodeName    = endpoint.info.name.getOrElse(headOfTags)
                val requestLogging = RequestLogging(
                  new Date(),
                  BuildInfo.name,
                  BuildInfo.version,
                  InetAddress.getLocalHost.toString,
                  requestId,
                  request.method.method,
                  methodeName,
                  request.uri.toString(),
                  remoteAddress.getOrElse("NOT_SET"),
                  user,
                  new org.joda.time.Duration(requestStart, new DateTime()).getMillis,
                  response.code.code,
                  controllerName,
                  methodeName,
                  endpoint.info.description.getOrElse("NOT_SET")
                )
                val insertResponse = MongoDatabase.requestLoggingDao.insertOne(requestLogging).result()
                histogram
              })
            }
            .onException { (endpoint, exception) =>
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
                val requestLogging = RequestLogging(
                  new Date(),
                  BuildInfo.name,
                  BuildInfo.version,
                  InetAddress.getLocalHost.toString,
                  requestId,
                  request.method.method,
                  methodeName,
                  request.uri.toString(),
                  remoteAddress.getOrElse("NOT_SET"),
                  user,
                  new org.joda.time.Duration(requestStart, new DateTime()).getMillis,
                  statusCode,
                  controllerName,
                  methodeName,
                  endpoint.info.description.getOrElse("NOT_SET")
                )
                MongoDatabase.requestLoggingDao.insertOne(requestLogging).asFuture()
                histogram
              })
            }
        }
      }
    )

}
