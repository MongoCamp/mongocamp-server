package dev.mongocamp.server.event.listener

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.event.Event
import dev.mongocamp.server.event.http.HttpRequestCompletedEvent
import dev.mongocamp.server.monitoring.MetricsConfiguration

import java.time.Duration

class MetricsLoggingActor extends Actor with LazyLogging {
  def receive: Receive = {

    case "info" =>
      logger.info(this.getClass.getSimpleName)

    case e: HttpRequestCompletedEvent =>
      val eventNameArray = e.getClass.getName.split('.')
      val indexOfEvent   = eventNameArray.indexOf("event")
      val pathArray      = eventNameArray.slice(indexOfEvent, eventNameArray.length)
      val regex          = "([a-z])([A-Z]+)"
      val replacement    = "$1.$2"
      val metricsName    = s"${pathArray.mkString(".")}".replaceAll("Event", "").replaceAll(regex, replacement).toLowerCase()
      MetricsConfiguration.getEventMetricsRegistries.foreach(_.timer(metricsName).record(Duration.ofMillis(e.duration.getMillis)))
      MetricsConfiguration.getEventMetricsRegistries.foreach(
        _.timer(s"$metricsName.${e.controller.toLowerCase()}.${e.controllerMethod.toLowerCase()}").record(Duration.ofMillis(e.duration.getMillis))
      )

    case e: Event =>
      val eventNameArray = e.getClass.getName.split('.')
      val indexOfEvent   = eventNameArray.indexOf("event")
      val pathArray      = eventNameArray.slice(indexOfEvent, eventNameArray.length)
      val regex          = "([a-z])([A-Z]+)"
      val replacement    = "$1.$2"
      val metricsName    = s"${pathArray.mkString(".")}".replaceAll("Event", "").replaceAll(regex, replacement).toLowerCase()
      MetricsConfiguration.getEventMetricsRegistries.foreach(_.summary(metricsName).record(1))

  }

}
