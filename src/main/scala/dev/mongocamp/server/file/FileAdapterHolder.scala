package dev.mongocamp.server.file

import dev.mongocamp.server.config.ConfigHolder
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.server.PluginLoadedEvent
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.service.ReflectionService
import sttp.model.StatusCode

object FileAdapterHolder {

  def isGridfsHolder: Boolean = ConfigHolder.fileHandlerType.value.equalsIgnoreCase("gridfs")

  lazy val listOfFilePlugins: List[FilePlugin] = ReflectionService
    .instancesForType(classOf[FilePlugin])
    .filterNot(plugin => ConfigHolder.pluginsIgnored.value.contains(plugin.getClass.getName))
    .map(plugin => {
      EventSystem.eventStream.publish(PluginLoadedEvent(plugin.getClass.getName, "FilePlugin"))
      plugin
    })

  lazy val handler: FilePlugin = {
    listOfFilePlugins
      .find(_.name.equalsIgnoreCase(ConfigHolder.fileHandlerType.value))
      .getOrElse(throw MongoCampException("Unknown File Handler defined", StatusCode.InternalServerError))
  }

}
