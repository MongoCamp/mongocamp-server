package dev.mongocamp.server.graal.substitution

import com.oracle.svm.core.annotate.{Substitute, TargetClass}
import dev.mongocamp.server.service.PluginService

import java.net.URL

@TargetClass(value = classOf[PluginService])
final class PluginServiceSubstitutions {

  @Substitute
  def validateFileForReflection(url: URL): Boolean = {
    url.toString.endsWith("jar")
  }

}
