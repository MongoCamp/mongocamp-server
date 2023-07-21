package dev.mongocamp.server.file


import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.server.PluginLoadedEvent
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.plugin.FilePlugin
import dev.mongocamp.server.service.{ConfigurationService, ReflectionService}
import sttp.model.StatusCode

object FileAdapterHolder {

  def isGridfsHolder: Boolean = handler.isInstanceOf[GridFsFileAdapter]

  lazy val listOfFilePlugins: List[FilePlugin] = ReflectionService
    .instancesForType(classOf[FilePlugin])
    .filterNot(plugin => ConfigurationService.getConfigValue[List[String]](DefaultConfigurations.ConfigKeyPluginsIgnored).contains(plugin.getClass.getName))
    .map(plugin => {
      EventSystem.eventStream.publish(PluginLoadedEvent(plugin.getClass.getName, "FilePlugin"))
      plugin
    })

  lazy val handler: FilePlugin = {
    listOfFilePlugins
      .find(_.name.equalsIgnoreCase(ConfigurationService.getConfigValue[String](DefaultConfigurations.ConfigKeyFileHandler)))
      .getOrElse(throw MongoCampException("Unknown File Handler defined", StatusCode.InternalServerError))
  }

}
