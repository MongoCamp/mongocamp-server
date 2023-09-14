package dev.mongocamp.server.cli.service

import dev.mongocamp.server.library.BuildInfo
import dev.mongocamp.server.service.CoursierModuleService

object JvmStartService {

  def startServer(): Int = {
    scala.util.Properties.osName
    val jars       = CoursierModuleService.loadServerWithAllDependencies()
    val runCommand = s"${JvmService.javaHome}/bin/java -cp ${jars.mkString(":")} ${BuildInfo.mainClass}"
    ProcessExecutorService.executeToStout(runCommand)
  }

  def startServerWithAgent(path: String): Int = {
    val jars       = CoursierModuleService.loadServerWithAllDependencies()
    val runCommand = s"${JvmService.javaHome}/bin/java -cp ${jars.mkString(":")} -agentlib:native-image-agent=config-output-dir=$path ${BuildInfo.mainClass}"
    ProcessExecutorService.executeToStout(runCommand)
  }

}
