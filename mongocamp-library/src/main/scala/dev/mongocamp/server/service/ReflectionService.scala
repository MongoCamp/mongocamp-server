package dev.mongocamp.server.service

import io.github.classgraph.{ ClassGraph, ScanResult }

import scala.jdk.CollectionConverters._
import scala.reflect.runtime.universe.runtimeMirror
import scala.util.Try

// todo https://github.com/classgraph/classgraph/wiki/Code-examples
// https://github.com/SoftInstigate/classgraph-on-graalvm
// dev.mongocamp.server.service.ReflectionService$
class ReflectionService {
  private val classGraph: ClassGraph = inizializeClassGraph()
  private var scanResult: ScanResult = scan()

  def inizializeClassGraph(): ClassGraph = {
    new ClassGraph().enableAllInfo().addClassLoader(ClassLoader.getSystemClassLoader)
  }

  def scan(): ScanResult = {
    classGraph.scan()
  }

  def instancesForType[T <: Any](clazz: Class[T]): List[T] = {
    val reflected = getSubClassesList(clazz).flatMap(foundClazz => {
      val clazzInstance = Try(foundClazz.getDeclaredConstructor().newInstance()).toOption
      if (clazzInstance.isDefined) {
        clazzInstance.map(_.asInstanceOf[T])
      }
      else {
        lazy val mirror = runtimeMirror(foundClazz.getClassLoader)
        val instance    = Try(mirror.reflectModule(mirror.moduleSymbol(foundClazz)).instance).toOption
        instance.map(_.asInstanceOf[T])
      }
    })
    reflected
  }

  def getSubClassesList[T <: Any](clazz: Class[T]): List[Class[_ <: T]] = {
    val subClasses = scanResult.getClassesImplementing(clazz).asScala.toList ++ scanResult.getSubclasses(clazz).asScala.toList
    subClasses.map(_.loadClass().asInstanceOf[Class[_ <: T]])
  }

  def registerClassLoaders[T <: Any](clazz: Class[T]): Unit = {
    registerClassLoaders(clazz.getClassLoader)
  }

  def registerClassLoaders(classLoader: ClassLoader): Unit = {
    classGraph.addClassLoader(classLoader)
    if (classLoader.getParent != null) {
      registerClassLoaders(classLoader.getParent)
      scanResult = scan()
    }
  }

  def getClassListByInterfaceName(className: String): List[Class[_]] = {
    scanResult.getClassesImplementing(className).asScala.toList.map(_.loadClass())
  }

  def getClassByName(className: String): Class[_] = {
    Try(scanResult.getClassInfo(className).loadClass()).toOption.getOrElse(throw new ClassNotFoundException())
  }

}

object ReflectionService {
  private val reflectionService = new ReflectionService()
  def instancesForType[T <: Any](clazz: Class[T]): List[T] = {
    reflectionService.instancesForType(clazz)
  }
  def getSubClassesList[T <: Any](clazz: Class[T]): List[Class[_ <: T]] = {
    reflectionService.getSubClassesList(clazz)
  }
  def registerClassLoaders[T <: Any](clazz: Class[T]): Unit = {
    reflectionService.registerClassLoaders(clazz)
  }
  def registerClassLoaders(classLoader: ClassLoader): Unit = {
    reflectionService.registerClassLoaders(classLoader)
  }
  def getClassListByInterfaceName(className: String): List[Class[_]] = {
    reflectionService.getClassListByInterfaceName(className)
  }
  def getClassByName(className: String): Class[_] = {
    reflectionService.getClassByName(className)
  }
}
