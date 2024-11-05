package dev.mongocamp.server.cli.service

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.library.BuildInfo
import dev.mongocamp.server.service.CoursierModuleService

object JvmStartService extends LazyLogging{

  def startServer(): scala.sys.process.Process = {
    val jars       = CoursierModuleService.loadServerWithAllDependencies()
    val runCommand = s"${JvmService.javaHome}/bin/java -cp ${jars.mkString(":")} ${BuildInfo.mainClass}"
    logger.error(s"Command to execute: $runCommand")
    ProcessExecutorService.stoutProcessBuilder(runCommand)
  }

  def startServerWithAgent(path: String): Int = {
    val jars       = CoursierModuleService.loadServerWithAllDependencies()
    val runCommand = s"${JvmService.javaHome}/bin/java -cp ${jars.mkString(":")} -agentlib:native-image-agent=config-output-dir=$path ${BuildInfo.mainClass}"
    ProcessExecutorService.executeToStout(runCommand)
  }

}
