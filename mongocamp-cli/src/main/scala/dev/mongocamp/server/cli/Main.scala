package dev.mongocamp.server.cli

import dev.mongocamp.server.library.BuildInfo
import dev.mongocamp.server.service.ConfigurationRead
import lukfor.progress.TaskService
import picocli.CommandLine
import picocli.CommandLine.Help.Ansi.Style
import picocli.CommandLine.Help.ColorScheme

object Main {

  private lazy val colorScheme = new ColorScheme.Builder()
    .commands(Style.bold, Style.underline) // combine multiple styles
    .options(Style.fg_yellow)              // yellow foreground color
    .parameters(Style.fg_yellow)
    .optionParams(Style.italic)
    .errors(Style.fg_red, Style.bold)
    .stackTraces(Style.italic)
    .applySystemProperties() // optional: allow end users to customize
    .build();

  lazy val commandLine: CommandLine = new CommandLine(new MongoCampServerCli()).setColorScheme(colorScheme)

  def main(args: Array[String]): Unit = {
    System.setProperty("NAME", BuildInfo.name)
    System.setProperty("VERSION", BuildInfo.version)
    TaskService.setAnsiSupport(true)
    System.exit(commandLine.execute(args: _*))
  }

}
