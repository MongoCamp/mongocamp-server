package dev.mongocamp.server.service

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.service.ReflectionService.registerClassLoaders
import org.reflections.vfs.Vfs

import scala.collection.mutable.ArrayBuffer
import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader

object PluginService extends LazyLogging {

  private def getChildFiles(dir: File): List[File] = {
    if (dir.isDirectory) {
      val files = ArrayBuffer[File]()
      dir.children.toList.foreach(file => {
        if (file.isDirectory) {
          files ++= getChildFiles(file)
        }
        else {
          files += file
        }
      })
      files.toList
    }
    else {
      List()
    }
  }

  def loadPlugins(): Unit = {
    registerClassLoaders(getClass)
    registerClassLoaders(ReflectionService.getClass)
    val pluginDirectory = File(ConfigurationService.getConfigValue[String](DefaultConfigurations.ConfigKeyPluginsDirectory))
    if (pluginDirectory.isDirectory) {
      val listUrl = getChildFiles(pluginDirectory)
        .map(_.url)
        .filter(url =>
          try {
            Vfs.fromURL(url)
            true
          }
          catch {
            case _: Exception =>
              false
          }
        )
      if (listUrl.nonEmpty) {
        val urlClassLoader = new URLClassLoader(listUrl.toSeq, this.getClass.getClassLoader)
        registerClassLoaders(urlClassLoader)
      }
    }
  }

  def downloadPlugins(): Unit = {
    val pluginDirectory = File(ConfigurationService.getConfigValue[String](DefaultConfigurations.ConfigKeyPluginsDirectory))
    if (pluginDirectory.notExists) {
      pluginDirectory.createDirectory()
    }
    if (pluginDirectory.isWritable) {
      val managedPluginDirectory = pluginDirectory.createChild("managed", asDirectory = true, createParents = true)
      val pluginsToDownload      = ConfigurationService.getConfigValue[List[String]](DefaultConfigurations.ConfigKeyPluginsUrls)
      managedPluginDirectory.children.foreach(_.delete())
      pluginsToDownload.foreach(pluginUrl => {
        try
          HttpClientService.downloadToFile(pluginUrl, managedPluginDirectory)
        catch {
          case e: MongoCampException => logger.error(s"Error on downloading plugin from $pluginUrl with following error message ${e.getMessage}")
        }
      })
    }
    else {
      logger.error(s"could not manage plugins from server side. ${pluginDirectory.toString()} is not writeable.")
    }
  }

}
