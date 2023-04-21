package dev.mongocamp.server.event.listener

import java.util.Date

case class DatabaseRequestLoggingElement(
    date: Date,
    applicationName: String,
    version: String,
    containerName: String,
    requestId: String,
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
