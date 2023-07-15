package dev.mongocamp.server.cli

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.cli.service.{JvmStartService, ProcessExecutorService}
import dev.mongocamp.server.service.PluginService
import picocli.CommandLine.Help.Ansi
import picocli.CommandLine.{Command, Parameters}

@Command(
  name = "run",
  description = Array("Check current Version of Dockerfile and File Version from Repository")
)
class StartApplicationCommand extends Runnable with LazyLogging {

  @Parameters(description = Array("StartUp Mode (Possible Values: <default>, <jvm> or <stable>"), defaultValue = "default")
  var mode: String = "default"

  def run(): Unit = {
    println(Ansi.AUTO.string(s"@|yellow Start MongoCamp Server with $mode|@"))
    mode match {
      case s: String if s.equalsIgnoreCase("jvm") => JvmStartService.startServer()

      case s: String if s.equalsIgnoreCase("native") =>
        val pluginService = new PluginService()
        val pluginUrls    = pluginService.listOfReadableUrls().map(url => File(url))
        if (pluginUrls.nonEmpty) {
          Main.commandLine.execute(List("buildNative"): _*)
          ProcessExecutorService.executeToStout("./server-with-plugins")
        }
        else {
          ProcessExecutorService.executeToStout("./server-raw")
        }

      case s: String if s.equalsIgnoreCase("default") =>
        val pluginService = new PluginService()
        val pluginUrls    = pluginService.listOfReadableUrls().map(url => File(url))
        if (pluginUrls.nonEmpty) {
          JvmStartService.startServer()
        }
        else {
          ProcessExecutorService.executeToStout("./server-raw")
        }

      case _ => println(Ansi.AUTO.string(s"@|bold,red Could not found definition for Mode $mode|@"))

    }
  }

}
