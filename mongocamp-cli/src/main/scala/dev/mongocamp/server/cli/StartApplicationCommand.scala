package dev.mongocamp.server.cli

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.cli.service.{JvmStartService, ProcessExecutorService, ServerService}
import dev.mongocamp.server.service.PluginService
import picocli.CommandLine.Help.Ansi
import picocli.CommandLine.{Command, Parameters}

import java.util.concurrent.Callable
import scala.sys.process

@Command(
  name = "run",
  description = Array("Check current Version of Dockerfile and File Version from Repository")
)
class StartApplicationCommand extends Callable[Integer] with LazyLogging {

  @Parameters(description = Array("StartUp Mode (Possible Values: <default>, <jvm> or <stable>"), defaultValue = "default")
  var mode: String = "default"

  def call(): Integer = {
    ServerService.startServer(mode)
  }

}
