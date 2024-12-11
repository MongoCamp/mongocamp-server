package dev.mongocamp.server.service.reflection

import com.typesafe.scalalogging.LazyLogging
import io.github.classgraph.{ ClassGraph, ClassInfo, ScanResult }

import scala.jdk.CollectionConverters._
import scala.reflect.runtime.universe.runtimeMirror
import scala.util.Try

class ClassGraphService extends LazyLogging {
  var scanResult: ScanResult = _

  private lazy val classGraph: ClassGraph = {
    val cG = new ClassGraph()
      .enableClassInfo()
      .disableModuleScanning()
      .addClassLoader(ClassLoader.getSystemClassLoader)
    ScanResult.fromJSON(cG.scan().toJSON)
    cG
  }

  private def scan(): ScanResult = {
    classGraph.scan()
  }

  private def getScanResult(): ScanResult = {
    if (scanResult == null) {
      scanClassPath()
    }
    scanResult
  }

  def scanClassPath(): Unit = {
    scanResult = scan()
  }

  def instancesForType[T <: Any](className: String): List[T] = {
    val subClassList = getSubClassesList(className)
    initializeClasses(subClassList)
  }

  def instancesForType[T <: Any](clazz: Class[T]): List[T] = {
    val subClassList = getSubClassesList(clazz)
    initializeClasses(subClassList)
  }

  private def initializeClasses[T <: Any](subClassList: List[Class[_ <: T]]): List[T] = {
    val reflected = subClassList.flatMap(
      foundClazz => {
        val clazzInstance = Try(foundClazz.getDeclaredConstructor().newInstance()).toOption
        if (clazzInstance.isDefined) {
          clazzInstance.map(_.asInstanceOf[T])
        }
        else {
          lazy val mirror = runtimeMirror(foundClazz.getClassLoader)
          val instance = Try(mirror.reflectModule(mirror.moduleSymbol(foundClazz)).instance).toOption
          instance.map(i => {
            if (i.isInstanceOf[T]) {
              i.asInstanceOf[T]
            } else {
                throw new ClassCastException(s"Class ${foundClazz.getName} is not a subclass")
            }
          })
        }
      }
    )
    reflected
  }

  def getSubClassesList[T <: Any](className: String): List[Class[_ <: T]] = {
    logger.trace(s"Count scanned classes ${getScanResult().getAllClasses.size()}")
    val classInfoList    = getScanResult().getClassesImplementing(className).asScala.toList
    val subClassInfoList = getScanResult().getSubclasses(className).asScala.toList
    logger.trace(s"Found ${classInfoList.size} classes implementing and ${subClassInfoList.size} subclasses for ${className}")
    createInstancesForClassInfos(classInfoList ++ subClassInfoList)
  }

  def getSubClassesList[T <: Any](clazz: Class[T]): List[Class[_ <: T]] = {
    logger.trace(s"Count scanned classes ${getScanResult().getAllClasses.size()}")
    val classInfoList    = getScanResult().getClassesImplementing(clazz).asScala.toList
    val subClassInfoList = getScanResult().getSubclasses(clazz).asScala.toList
    logger.trace(s"Found ${classInfoList.size} classes implementing and ${subClassInfoList.size} subclasses for ${clazz.getName}")
    createInstancesForClassInfos(classInfoList ++ subClassInfoList)
  }

  def registerClassLoaders[T <: Any](clazz: Class[T]): Unit = {
    registerClassLoaders(clazz.getClassLoader)
  }

  def registerClassLoaders(classLoader: ClassLoader): Unit = {
    classGraph.addClassLoader(classLoader)
    if (classLoader.getParent != null) {
      registerClassLoaders(classLoader.getParent)
      scanClassPath()
    }
  }

  def getClassListByInterfaceName(className: String): List[Class[_]] = {
    getScanResult().getClassesImplementing(className).asScala.toList.map(_.loadClass())
  }

  def getClassByName(className: String): Class[_] = {
    Try(getScanResult().getClassInfo(className).loadClass()).toOption.getOrElse(throw new ClassNotFoundException())
  }

  def getClassesForPackage[T <: Any](packageName: String): List[Class[_ <: T]] = {
    val subClasses = getScanResult().getAllClasses.asScala.filter(_.getPackageName.startsWith(packageName)).toList
    subClasses.map(_.loadClass().asInstanceOf[Class[_ <: T]])
  }

  private def createInstancesForClassInfos[T <: Any](classInfoList: List[ClassInfo]): List[Class[_ <: T]] = {
    classInfoList.flatMap(
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
}
