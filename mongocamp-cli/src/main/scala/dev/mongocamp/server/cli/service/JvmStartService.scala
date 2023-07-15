package dev.mongocamp.server.cli.service

import dev.mongocamp.server.library.BuildInfo
import dev.mongocamp.server.service.CoursierModuleService

object JvmStartService {
  private val javaHome = System.getProperty("java.home")

  def startServer(): Unit = {
    val jars = CoursierModuleService.loadServerWithAllDependencies()
    val runCommand = s"$javaHome/bin/java -cp ${jars.mkString(":")} ${BuildInfo.mainClass}"
    ProcessExecutorService.executeToStout(runCommand)
  }

  def startServerWithAgent(path: String): Unit = {
    val jars = CoursierModuleService.loadServerWithAllDependencies()
    val runCommand = s"$javaHome/bin/java -cp ${jars.mkString(":")} -agentlib:native-image-agent=config-output-dir=$path ${BuildInfo.mainClass}"
    ProcessExecutorService.executeToStout(runCommand)
  }

}
