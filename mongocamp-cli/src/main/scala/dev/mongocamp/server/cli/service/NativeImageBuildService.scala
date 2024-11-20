package dev.mongocamp.server.cli.service

import better.files.File
import dev.mongocamp.server.cli.exception.NativeBuildException
import dev.mongocamp.server.library.BuildInfo

object NativeImageBuildService {

  def prepareSystemForBuildingNativeImage(): Unit = {
    if (System.getProperty("os.name").toLowerCase.contains("linux")) {
      val installCommand = s"apt-get -y install build-essential libz-dev zlib1g-dev;"
      ProcessExecutorService.executeToStout(installCommand)
      installNativeImageExecutable()
    }
  }

  def installNativeImageExecutable(): Unit = {
    val installCommand = s"${JvmService.javaHome}/bin/gu install native-image"
    ProcessExecutorService.executeToString(installCommand)
  }

  def buildNativeImage(jars: List[File], imageName: String, waitForDebug : Boolean = false): String = {
    var buildOptions = List(
      "--no-fallback",
      "--verbose",
      "--native-image-info",
      "-H:+DumpOnError",
//      "-H:+ReportExceptionStackTraces",
//      "-H:+ReportUnsupportedElementsAtRuntime",
//      "-H:+PrintAnalysisCallTree",
//      "-H:Log=registerResource:5",
      "--report-unsupported-elements-at-runtime",
//      "--trace-object-instantiation=java.io.File,java.util.jar.JarFile"
    )
    if (waitForDebug) {
      buildOptions = buildOptions ++ List("--debug-attach")
    }
    val tempDir = File.newTemporaryDirectory()
    val classPath = (jars ++ List(tempDir.toString()))
    val generateCommand = s"${JvmService.javaHome}/bin/native-image ${buildOptions.mkString(" ")} -cp ${classPath.mkString(":")} ${BuildInfo.mainClass} $imageName"
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
    println(s"Runnable Path after build: $runnableResponse")
    println(s"TempDir: $tempDir")
//    tempDir.delete()
    runnableResponse
  }

}
