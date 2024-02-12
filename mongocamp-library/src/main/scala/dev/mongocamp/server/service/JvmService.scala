package dev.mongocamp.server.service

object JvmService {

  def runntimeJavaHome: Option[String] = {
    Option(System.getProperty("java.home")).map(
      jHome => {
        var javaHome = jHome
        while (javaHome.startsWith(" ") || javaHome.endsWith(" ")) {
          javaHome = javaHome.trim
          javaHome = javaHome.trim
        }
        javaHome
      }
    )
  }

  def javaHome: String = {
    val envValue = System.getenv("JAVA_HOME")
    var javaHome: String = if (runntimeJavaHome.isDefined) {
      runntimeJavaHome.get
    }
    else if (envValue != null) {
      envValue
    }
    else {
      ""
    }

    while (javaHome.startsWith(" ") || javaHome.endsWith(" ")) {
      javaHome = javaHome.trim
      javaHome = javaHome.trim
    }
    javaHome
  }

  def isNativeImage: String = {
    javaHome
  }
}
