package dev.mongocamp.server.service

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.exception.MongoCampException

class PluginDownloadService extends LazyLogging {

  def downloadPlugins(): Unit = {
    val pluginDirectory = File(ConfigurationRead.noPublishReader.getConfigValue[String](DefaultConfigurations.ConfigKeyPluginsDirectory))
    if (pluginDirectory.notExists) {
      pluginDirectory.toJava.mkdirs()
    }
    if (pluginDirectory.isWritable) {
      val managedPluginDirectory = pluginDirectory.createChild("managed", asDirectory = true, createParents = true)
      val pluginsToDownload      = ConfigurationRead.noPublishReader.getConfigValue[List[String]](DefaultConfigurations.ConfigKeyPluginsUrls)
      managedPluginDirectory.children.foreach(_.delete())
      pluginsToDownload.foreach(
        pluginUrl => {
          try
            HttpClientService.downloadToFile(pluginUrl, managedPluginDirectory)
          catch {
            case e: MongoCampException => logger.error(s"Error on downloading plugin from $pluginUrl with following error message ${e.getMessage}")
          }
        }
      )
    }
    else {
      logger.error(s"could not manage plugins from server side. ${pluginDirectory.toString()} is not writeable.")
    }
  }

}
