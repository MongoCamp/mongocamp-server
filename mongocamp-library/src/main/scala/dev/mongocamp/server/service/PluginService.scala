package dev.mongocamp.server.service

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.service.ReflectionService.registerClassLoaders

import java.net.URL
import scala.collection.mutable.ArrayBuffer
import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader

class PluginService extends LazyLogging {
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
    val listUrl = listOfReadableUrls()
    if (listUrl.nonEmpty) {
      val urlClassLoader = new URLClassLoader(listUrl, ClassLoader.getSystemClassLoader)
      registerClassLoaders(urlClassLoader)
    }
  }

  def listOfReadableUrls(): List[URL] = {
    val pluginDirectory = File(ConfigurationRead.noPublishReader.getConfigValue[String](DefaultConfigurations.ConfigKeyPluginsDirectory))
    val files = CoursierModuleService.loadMavenConfiguredDependencies() ++ (if (pluginDirectory.isDirectory) getChildFiles(pluginDirectory) else List.empty)
    files.map(_.url) //.filter(validateFileForReflection)
  }

}

object PluginService extends LazyLogging {
  private val service = new PluginService()

  def loadPlugins(): Unit = service.loadPlugins()

  def listOfReadableUrls(): List[URL] = service.listOfReadableUrls()

}
