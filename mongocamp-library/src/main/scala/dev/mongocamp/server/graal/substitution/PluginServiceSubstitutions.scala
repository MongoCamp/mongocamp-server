package dev.mongocamp.server.graal.substitution

import com.oracle.svm.core.annotate.{ Substitute, TargetClass }
import dev.mongocamp.server.service.PluginService

@TargetClass(value = classOf[PluginService])
final class PluginServiceSubstitutions {

  @Substitute
  def loadPlugins(): Unit = {
    println("Register Class Loader is not supported at native image")
  }

}
