package dev.mongocamp.server.service.reflection

import com.typesafe.scalalogging.LazyLogging
import io.github.classgraph.{ ClassGraph, ScanResult }

import scala.jdk.CollectionConverters._
import scala.reflect.runtime.universe.runtimeMirror
import scala.util.Try

class ClassGraphService extends LazyLogging {
  var scanResult: ScanResult = _

  private val classGraph: ClassGraph = {
    val cG = new ClassGraph()
      .enableClassInfo()
      .disableModuleScanning()
      .addClassLoader(ClassLoader.getSystemClassLoader)
    scanResult = scan()
    cG
  }

  private def scan(): ScanResult = {
    classGraph.scan()
  }

  def instancesForType[T <: Any](clazz: Class[T]): List[T] = {
    val subClassList = getSubClassesList(clazz)
    val reflected = subClassList.flatMap(
      foundClazz => {
        val clazzInstance = Try(foundClazz.getDeclaredConstructor().newInstance()).toOption
        if (clazzInstance.isDefined) {
          clazzInstance.map(_.asInstanceOf[T])
        }
        else {
          lazy val mirror = runtimeMirror(foundClazz.getClassLoader)
          val instance    = Try(mirror.reflectModule(mirror.moduleSymbol(foundClazz)).instance).toOption
          instance.map(_.asInstanceOf[T])
        }
      }
    )
    reflected
  }

  def getSubClassesList[T <: Any](clazz: Class[T]): List[Class[_ <: T]] = {
    logger.trace(s"Count scanned classes ${scanResult.getAllClasses.size()}")
    val classInfoList    = scanResult.getClassesImplementing(clazz).asScala.toList
    val subClassInfoList = scanResult.getSubclasses(clazz).asScala.toList
    logger.trace(s"Found ${classInfoList.size} classes implementing and ${subClassInfoList.size} subclasses for ${clazz.getName}")
    (classInfoList ++ subClassInfoList).flatMap(
      c => {
        val option =
          try {
            logger.trace(s"Try to loads class ${c.getName}")
            val internal = Option(c.loadClass()).map(_.asInstanceOf[Class[_ <: T]])
            if (internal.isEmpty) {
              logger.trace(s"Class ${c.getName} could not be loaded")
            }
            internal
          }
          catch {
            case t: Throwable =>
              logger.error(s"Error loading class ${c.getName}", t)
              None
          }
        option
      }
    )
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

  def getClassesForPackage[T <: Any](packageName: String): List[Class[_ <: T]] = {
    val subClasses = scanResult.getAllClasses.asScala.filter(_.getPackageName.startsWith(packageName)).toList
    subClasses.map(_.loadClass().asInstanceOf[Class[_ <: T]])
  }

}
