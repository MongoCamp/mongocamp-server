package dev.mongocamp.server.cli.service

import better.files.File
import dev.mongocamp.server.cli.exception.NativeBuildException
import dev.mongocamp.server.library.BuildInfo
object NativeImageBuildService {

  def installNativeImageExecutable(): Unit = {
    val installCommand = s"${JvmService.javaHome}/bin/gu install native-image"
    ProcessExecutorService.executeToString(installCommand)
  }

  def buildNativeImage(jars: List[File], imageName: String): String = {
    val buildOptions = List(
      "--no-fallback",
      "--verbose",
      "-H:+ReportExceptionStackTraces",
      "-H:+ReportUnsupportedElementsAtRuntime",
      "-H:+ReportExceptionStackTraces",
      "--report-unsupported-elements-at-runtime"
    )
    val generateCommand = s"${JvmService.javaHome}/bin/native-image ${buildOptions.mkString(" ")} -cp ${jars.mkString(":")} ${BuildInfo.mainClass} $imageName"
    val result          = ProcessExecutorService.executeToString(generateCommand)
    if (result.contains(s"Failed generating '${imageName}'")) {
      throw NativeBuildException(result)
    }
    val runnableRegex    = "Produced artifacts:\n(.*?) \\(executable\\)".r
    val matches          = runnableRegex.findAllMatchIn(result).toList
    var runnableResponse = matches.last.matched.replaceAll("Produced artifacts:\n", "").replaceAll("\\(executable\\)", "")
    while (runnableResponse.startsWith(" ") || runnableResponse.endsWith(" ")) {
      runnableResponse = runnableResponse.trim
      runnableResponse = runnableResponse.trim
    }
    println(s"Runnable Path after build: ${runnableResponse}")
    runnableResponse
  }

}
