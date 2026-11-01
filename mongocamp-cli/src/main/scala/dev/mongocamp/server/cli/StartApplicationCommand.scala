package dev.mongocamp.server.cli

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.cli.service.JvmStartService
import dev.mongocamp.server.cli.service.ProcessExecutorService
import dev.mongocamp.server.cli.service.ServerService
import dev.mongocamp.server.service.PluginService
import java.util.concurrent.Callable
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Ansi
import picocli.CommandLine.Parameters
import scala.sys.process

@Command(name = "run", description = Array("Check current Version of Dockerfile and File Version from Repository"))
class StartApplicationCommand extends Callable[Integer] with LazyLogging {

  @Parameters(description = Array("StartUp Mode (Possible Values: <default>, <jvm> or <stable>"), defaultValue = "default")
  var mode: String = "default"

  def call(): Integer = {
    ServerService.startServer(mode)
  }

}
