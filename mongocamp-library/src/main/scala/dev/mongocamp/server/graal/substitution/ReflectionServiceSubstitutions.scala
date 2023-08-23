package dev.mongocamp.server.graal.substitution

import com.oracle.svm.core.annotate.{Substitute, TargetClass}
import dev.mongocamp.server.service.ReflectionService
import io.github.classgraph.{ClassGraph, ScanResult}

@TargetClass(value = classOf[ReflectionService])
final class ReflectionServiceSubstitutions {

  @Substitute
  def inizializeClassGraph(): ClassGraph = {
//    val classGraph = new ClassGraph()
//      .enableAllInfo()
//      .addClassLoader(ClassLoader.getSystemClassLoader)
//      .disableNestedJarScanning()
//      .disableModuleScanning()
//      .disableDirScanning()
//      .disableRuntimeInvisibleAnnotations()
//      .initializeLoadedClasses()
//    classGraph
    null
  }

  @Substitute
  def registerClassLoaders(classLoader: ClassLoader): Unit = {
    println("Register Class Loader is not supported at native image")
  }

  @Substitute
  def registerClassLoaders[T <: Any](clazz: Class[T]): Unit = {
    println("Register Class Loader is not supported at native image")
  }


  @Substitute
  def scan(): ScanResult = {
    ScanResult.fromJSON("{}")
  }
}
