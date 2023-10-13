package dev.mongocamp.server.interceptor

import org.apache.pekko.http.scaladsl.model.AttributeKey
import org.apache.pekko.http.scaladsl.server.RequestContext
import sttp.tapir.model.ServerRequest

import scala.jdk.OptionConverters._

object RequestFunctions {
  val mongoCampRequestIdKey = "x-request-id"
  val requestIdAttributeKey = AttributeKey[String](mongoCampRequestIdKey)

  val requestHeaderKeyRealIp        = "X-Real-Ip"
  val requestHeaderKeyRemoteAddress = "Remote-Address"

  def getRequestIdOption(request: ServerRequest): Option[String] = {
    request.underlying
      .asInstanceOf[RequestContext]
      .request
      .getAttribute(requestIdAttributeKey)
      .toScala
  }
}
