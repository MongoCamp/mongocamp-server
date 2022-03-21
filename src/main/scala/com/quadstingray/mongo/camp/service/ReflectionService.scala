package com.quadstingray.mongo.camp.service
import better.files.File
import com.quadstingray.mongo.camp.config.Config
import org.reflections.Reflections
import org.reflections.util.{ ClasspathHelper, ConfigurationBuilder }

import java.net.URL
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._
import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader
import scala.reflect.runtime.universe.runtimeMirror
import scala.util.Try

object ReflectionService extends Config {
  private val reflectionConfigurationBuilder: ConfigurationBuilder = new ConfigurationBuilder().forPackages("")

  def instancesForType[T <: Any](clazz: Class[T]): List[T] = {
    val urls: ArrayBuffer[URL] = ArrayBuffer[URL]()
    urls.addAll(ClasspathHelper.forJavaClassPath().asScala)
    reflectionConfigurationBuilder.getClassLoaders.foreach(classLoader => {
      classLoader match {
        case loader: URLClassLoader => urls.addAll(loader.getURLs)
        case _                      => classLoader.getDefinedPackages.foreach(p => urls.addAll(ClasspathHelper.forPackage(p.toString, classLoader).asScala))
      }
      ""
    })
    reflectionConfigurationBuilder.addUrls(urls.asJava)
    val reflected = new Reflections(reflectionConfigurationBuilder)
      .getSubTypesOf(clazz)
      .asScala
      .flatMap(foundClazz => {
        lazy val mirror   = runtimeMirror(foundClazz.getClassLoader)
        val instance      = Try(mirror.reflectModule(mirror.moduleSymbol(foundClazz)).instance).toOption
        val clazzInstance = Try(foundClazz.getDeclaredConstructor().newInstance()).toOption
        if (clazzInstance.isDefined) {
          clazzInstance
        }
        else {
          instance.map(_.asInstanceOf[T])
        }
      })
      .toList
    reflected
  }

  def registerClassLoaders[T <: Any](clazz: Class[T]): Unit = {
    val classLoader = clazz.getClassLoader
    registerClassLoaders(classLoader)
  }

  def registerClassLoaders[T <: Any](classLoader: ClassLoader): Unit = {
    reflectionConfigurationBuilder.addClassLoaders(classLoader)
    if (classLoader.getParent != null) {
      registerClassLoaders(classLoader.getParent)
    }
  }

  def loadPlugins(): Unit = {
    registerClassLoaders(getClass)
    val pluginDirectory = File(globalConfigString("plugins.directory"))
    if (pluginDirectory.isDirectory) {
      val listUrl = pluginDirectory.children.map(_.url)
      if (listUrl.nonEmpty) {
        val urlClassLoader = new URLClassLoader(listUrl.toSeq, this.getClass.getClassLoader)
        registerClassLoaders(urlClassLoader)
      }
    }
  }

}
