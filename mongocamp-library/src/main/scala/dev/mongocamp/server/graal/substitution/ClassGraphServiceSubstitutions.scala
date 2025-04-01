package dev.mongocamp.server.graal.substitution

import better.files.Resource
import com.oracle.svm.core.annotate.{ Substitute, TargetClass }
import dev.mongocamp.server.service.reflection.ClassGraphService
import io.github.classgraph.ScanResult

@TargetClass(value = classOf[ClassGraphService])
final class ClassGraphServiceSubstitutions {

  @Substitute
  private def scan(): ScanResult = {
    ScanResult.fromJSON(Resource.getAsString("mongocamp-classes.json"))
  }

  @Substitute
  def registerClassLoaders(classLoader: ClassLoader): Unit = {
    println("Register Class Loader is not supported at native image")
  }

  @Substitute
  def registerClassLoaders[T <: Any](clazz: Class[T]): Unit = {
    println("Register Class Loader is not supported at native image")
  }

}
