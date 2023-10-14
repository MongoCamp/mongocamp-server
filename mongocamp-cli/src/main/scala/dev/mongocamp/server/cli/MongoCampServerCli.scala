package dev.mongocamp.server.cli

import dev.mongocamp.server.cli.prepare.PrepareSubcommands
import picocli.CommandLine
import picocli.CommandLine.{ Command, HelpCommand }

import java.util.concurrent.Callable

@Command(
  name = "${NAME}",
  version = Array("Version: ${VERSION}"),
  mixinStandardHelpOptions = true,
  subcommands = Array(classOf[StartApplicationCommand], classOf[BuildNativeImageCommand], classOf[PrepareSubcommands], classOf[HelpCommand]),
  description = Array("Cli for starting, compile and caching MongoCamp Server")
)
class MongoCampServerCli extends Callable[Integer] {

  @CommandLine.Spec
  val spec: CommandLine.Model.CommandSpec = null

  def call(): Integer = {
    throw new CommandLine.ParameterException(spec.commandLine(), "no command specified")
  }

}
