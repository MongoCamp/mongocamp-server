package dev.mongocamp.server.cli.prepare

import dev.mongocamp.server.cli.Main
import picocli.CommandLine
import picocli.CommandLine.{Command, HelpCommand}

import java.util.concurrent.Callable
@Command(
  name = "prepare",
  mixinStandardHelpOptions = true,
  subcommands = Array(classOf[InstallJVMCommand], classOf[CacheCommand], classOf[BuildNativeImageDefaultServerCommand], classOf[RunAgentCommand], classOf[HelpCommand]),
  description = Array("Commands to prepare Application")
)
class PrepareSubcommands extends Callable[Integer] {

  @CommandLine.Spec
  val spec: CommandLine.Model.CommandSpec = null

  def call(): Integer = {
    var response = 0
    response += Main.commandLine.execute(List("prepare", "jvm"): _*).abs
    response += Main.commandLine.execute(List("prepare", "cache"): _*).abs
    response += Main.commandLine.execute(List("prepare", "native"): _*).abs
    response
  }

}
