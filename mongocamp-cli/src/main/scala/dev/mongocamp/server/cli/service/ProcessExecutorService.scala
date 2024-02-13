package dev.mongocamp.server.cli.service

import picocli.CommandLine.Help.Ansi

import scala.sys.process._

object ProcessExecutorService {

  def executeToString(executable: String): String = {
    var completeResponse = ""

    def appendString(string: String, appendable: String) = {
      completeResponse = completeResponse + "\n" + appendable
      if (string.trim.isEmpty) {
        appendable
      }
      else {
        string + "\n" + appendable
      }
    }
    var responseString = ""
    var errorString    = ""
    val stringProcessLogger = ProcessLogger(stout => responseString = appendString(responseString, stout), sterr => errorString = appendString(errorString, sterr))
    val responseCode = executable.!(stringProcessLogger)
    if (responseCode != 0) {
      throw new RuntimeException(s"Error while executing command: $executable\n$responseString\n$errorString")
    }
    responseString
  }

  def executeToStout(executable: String): Int = {
    stoutProcessBuilder(executable).exitValue()
  }

  def stoutProcessBuilder(executable: String): Process = {
    val process = executable.run(stoutProcessLogger)
    process
  }

  def stoutProcessLogger: ProcessLogger = ProcessLogger(stout => println(Ansi.AUTO.string(s"@|green $stout|@")), sterr => println(Ansi.AUTO.string(s"@|red $sterr|@")))

}
