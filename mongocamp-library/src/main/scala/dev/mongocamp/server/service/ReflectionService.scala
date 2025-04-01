package dev.mongocamp.server.service

import dev.mongocamp.server.service.reflection.ClassGraphService

object ReflectionService {
  private val reflectionService = new ClassGraphService()

  def scanClassPath(): Unit = {
    reflectionService.scanClassPath()
  }

  def instancesForType[T <: Any](clazz: Class[T]): List[T] = {
    reflectionService.instancesForType(clazz)
  }

  def instancesForType[T <: Any](className: String): List[T] = {
    reflectionService.instancesForType(className)
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
