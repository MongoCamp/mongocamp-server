package dev.mongocamp.server.cli.prepare

import picocli.CommandLine
import picocli.CommandLine.{Command, HelpCommand}

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
    throw new CommandLine.ParameterException(spec.commandLine(), "Specify a subcommand")
  }

}
