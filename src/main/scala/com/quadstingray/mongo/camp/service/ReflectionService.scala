package com.quadstingray.mongo.camp.service
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder

import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.reflect.runtime.universe.runtimeMirror
import scala.util.Try

object ReflectionService {
  private val reflections = new Reflections(new ConfigurationBuilder().forPackages(""))

  def instancesForType[T <: Any](clazz: Class[T]): List[T] = {
    val reflected = reflections
      .getSubTypesOf(clazz)
      .asScala
      .flatMap(clazz => {
        lazy val mirror   = runtimeMirror(clazz.getClassLoader)
        val instance      = Try(mirror.reflectModule(mirror.moduleSymbol(clazz)).instance).toOption
        val clazzInstance = Try(clazz.getDeclaredConstructor().newInstance()).toOption
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
}
