package dev.mongocamp.server.event.http

case class HttpRequestStartEvent(
    requestId: String,
    httpMethod: String,
    methodName: String,
    uri: String,
    remoteAddress: String,
    userId: String,
    controller: String,
    controllerMethod: String,
    comment: String
) extends HttpRequestEvent
