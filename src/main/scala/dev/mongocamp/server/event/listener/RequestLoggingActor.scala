package dev.mongocamp.server.event.listener
import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.BuildInfo
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.event.http.{ HttpRequestCompletedEvent, HttpRequestStartEvent }
import dev.mongocamp.server.interceptor.RequestLogging

import java.net.InetAddress

class RequestLoggingActor extends Actor with LazyLogging {
  def receive: Receive = {

    case "info" =>
      logger.info(this.getClass.getSimpleName)

    case event: HttpRequestCompletedEvent =>
      val updateMap      = Map("$set" -> Map("duration" -> event.duration.getMillis, "responseCode" -> event.responseCode))
      val updateResponse = MongoDatabase.requestLoggingDao.updateOne(Map("requestId" -> event.requestId), updateMap).result()
      updateResponse

    case event: HttpRequestStartEvent =>
      val requestLogging = RequestLogging(
        event.eventDate,
        BuildInfo.name,
        BuildInfo.version,
        InetAddress.getLocalHost.toString,
        event.requestId,
        event.httpMethod,
        event.methodName,
        event.uri,
        event.remoteAddress,
        event.userId,
        -1,
        -1,
        event.controller,
        event.controllerMethod,
        event.comment
      )
      val insertResponse = MongoDatabase.requestLoggingDao.insertOne(requestLogging).result()
      insertResponse
  }

}
