package dev.mongocamp.server.event.http
import org.joda.time.Duration

case class HttpRequestCompletedEvent(
    requestId: Int,
    httpMethod: String,
    methodName: String,
    uri: String,
    remoteAddress: String,
    userId: String,
    duration: Duration,
    responseCode: Int,
    controller: String,
    controllerMethod: String,
    comment: String
) extends HttpRequestEvent
