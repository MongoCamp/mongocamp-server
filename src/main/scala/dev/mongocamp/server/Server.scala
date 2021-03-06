package dev.mongocamp.server
import dev.mongocamp.server.config.ConfigHolder
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.server.PluginLoadedEvent
import dev.mongocamp.server.plugin.RoutesPlugin
import dev.mongocamp.server.route._
import dev.mongocamp.server.service.ReflectionService

import scala.concurrent.ExecutionContext

object Server extends App with RestServer {

  implicit val ex: ExecutionContext = ActorHandler.requestExecutionContext

  lazy val listOfRoutePlugins: List[RoutesPlugin] = {
    ReflectionService
      .instancesForType(classOf[RoutesPlugin])
      .filterNot(plugin => ConfigHolder.pluginsIgnored.value.contains(plugin.getClass.getName))
      .map(plugin => {
        EventSystem.eventStream.publish(PluginLoadedEvent(plugin.getClass.getName, "RoutesPlugin"))
        plugin
      })
  }

  override lazy val serverEndpoints =
    InformationRoutes.routes ++ AuthRoutes.authEndpoints ++ AdminRoutes.endpoints ++ listOfRoutePlugins.flatMap(_.endpoints) ++ IndexRoutes.endpoints

  startServer()

}
