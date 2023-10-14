package dev.mongocamp.server.cli

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.cli.service.{ JvmStartService, ProcessExecutorService }
import dev.mongocamp.server.service.PluginService
import picocli.CommandLine.Help.Ansi
import picocli.CommandLine.{ Command, Parameters }

import java.util.concurrent.Callable

@Command(
  name = "run",
  description = Array("Check current Version of Dockerfile and File Version from Repository")
)
class StartApplicationCommand extends Callable[Integer] with LazyLogging {

  @Parameters(description = Array("StartUp Mode (Possible Values: <default>, <jvm> or <stable>"), defaultValue = "default")
  var mode: String = "default"

  def call(): Integer = {
    println(Ansi.AUTO.string(s"@|yellow Start MongoCamp Server with $mode|@"))
    mode match {
      case s: String if s.equalsIgnoreCase("jvm") => JvmStartService.startServer()

      case s: String if s.equalsIgnoreCase("native") =>
        val pluginUrls = PluginService.listOfReadableUrls().map(url => File(url))
        if (pluginUrls.nonEmpty) {
          var response = Main.commandLine.execute(List("buildNative"): _*).abs
          response += ProcessExecutorService.executeToStout("./server-with-plugins").abs
          response
        }
        else {
          ProcessExecutorService.executeToStout("./server-raw")
        }

      case s: String if s.equalsIgnoreCase("default") =>
        val pluginUrls = PluginService.listOfReadableUrls().map(url => File(url))
        if (pluginUrls.nonEmpty) {
          JvmStartService.startServer()
        }
        else {
          ProcessExecutorService.executeToStout("./server-raw")
        }

      case _ =>
        println(Ansi.AUTO.string(s"@|bold,red Could not found definition for Mode $mode|@"))
        1

    }
  }

}
