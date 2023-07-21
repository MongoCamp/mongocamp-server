package dev.mongocamp.server.cli.prepare

import dev.mongocamp.server.cli.Main
import picocli.CommandLine
import picocli.CommandLine.{ Command, HelpCommand }

@Command(
  name = "prepare",
  mixinStandardHelpOptions = true,
  subcommands = Array(classOf[CacheCommand], classOf[BuildNativeImageDefaultServerCommand], classOf[RunAgentCommand], classOf[HelpCommand]),
  description = Array("Commands to prepare Application")
)
class PrepareSubcommands extends Runnable {

  @CommandLine.Spec
  val spec: CommandLine.Model.CommandSpec = null

  def run(): Unit = {
    Main.commandLine.execute(List("prepare", "jvm"): _*)
    Main.commandLine.execute(List("prepare", "cache"): _*)
    Main.commandLine.execute(List("prepare", "native"): _*)
  }

}
