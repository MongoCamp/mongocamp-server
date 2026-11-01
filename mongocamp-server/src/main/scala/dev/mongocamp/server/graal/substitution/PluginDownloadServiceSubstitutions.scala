package dev.mongocamp.server.graal.substitution

import com.oracle.svm.core.annotate.Substitute
import com.oracle.svm.core.annotate.TargetClass
import dev.mongocamp.server.service.PluginDownloadService

@TargetClass(value = classOf[PluginDownloadService])
final class PluginDownloadServiceSubstitutions {

  @Substitute
  def downloadPlugins(): Unit = {
    println("Download Plugins is not supported at native image")
  }

}
