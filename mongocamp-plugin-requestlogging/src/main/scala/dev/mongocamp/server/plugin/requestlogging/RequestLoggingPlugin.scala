package dev.mongocamp.server.plugin.requestlogging

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.http.HttpRequestEvent
import dev.mongocamp.server.model.MongoCampConfiguration
import dev.mongocamp.server.plugin.ServerPlugin
import dev.mongocamp.server.plugin.requestlogging.listener.RequestLoggingActor
import dev.mongocamp.server.service.ConfigurationService
import org.apache.pekko.actor.Props

object RequestLoggingPlugin extends ServerPlugin with LazyLogging {
  lazy val ConfigKeyRequestLogging = "REQUESTLOGGING_ENABLED"

  override def activate(): Unit = {
    ConfigurationService.registerConfig(ConfigKeyRequestLogging, MongoCampConfiguration.confTypeBoolean, Option(true), needsRestartForActivation = true)
    if (ConfigurationService.getConfigValue(ConfigKeyRequestLogging)) {
      val requestLoggingActor = EventSystem.startActor(Props(classOf[RequestLoggingActor]), "requestLoggingActor")
      EventSystem.subscribe(requestLoggingActor, classOf[HttpRequestEvent])
    }
  }

}
