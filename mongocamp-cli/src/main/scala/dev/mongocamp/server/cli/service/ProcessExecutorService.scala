package dev.mongocamp.server.cli.service

import picocli.CommandLine.Help.Ansi

import scala.sys.process._

object ProcessExecutorService {

  def executeToString(executable: String): String = {
    executable.!!
  }

  def executeToStout(executable: String): Int = {
    executable.!(ProcessLogger(stout => println(Ansi.AUTO.string(s"@|green $stout|@")), sterr => println(Ansi.AUTO.string(s"@|red $sterr|@"))))
  }

}
