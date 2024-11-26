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

  def buildNativeImage(jars: List[File], imageName: String, waitForDebug: Boolean = false): String = {
    var buildOptions = List(
      "--no-fallback",
      "--verbose",
      "--native-image-info",
      "-H:+DumpOnError",
      "-H:+ReportExceptionStackTraces",
      "-H:+ReportUnsupportedElementsAtRuntime",
      "-H:+PrintAnalysisCallTree",
      "-H:Log=registerResource:5",
      "-H:IncludeResources=\".*mongocamp-classes.json$\""
//      "--trace-object-instantiation=java.io.File,java.util.jar.JarFile"
    )
    if (waitForDebug) {
      buildOptions = buildOptions ++ List("--debug-attach")
    }
    val tempDir   = File.newTemporaryDirectory()
    val classPath = (jars ++ List(tempDir.toString()))
    val generateCommand =
      s"${JvmService.javaHome}/bin/native-image ${buildOptions.mkString(" ")} -cp ${classPath.mkString(":")} ${BuildInfo.mainClass} $imageName"
    val result = ProcessExecutorService.executeToString(generateCommand)

    if (result.contains(s"Failed generating '${imageName}'")) {
      throw NativeBuildException(result)
    }

    val runnableMacRegex          = "Produced artifacts:\n(.*?) \\(executable\\)".r
    val matchesMac                = runnableMacRegex.findAllMatchIn(result).toList
    val runnableMacResponseOption = matchesMac.lastOption.map(_.matched.replaceAll("Produced artifacts:\n", "").replaceAll("\\(executable\\)", ""))

    val runnableLinuxRegex          = "Build artifacts:\n(.*?) \\(executable\\)".r
    val matchesLinux                = runnableLinuxRegex.findAllMatchIn(result).toList
    val runnableLinuxResponseOption = matchesLinux.lastOption.map(_.matched.replaceAll("Produced artifacts:\n", "").replaceAll("\\(executable\\)", ""))
    val runnableProcessList = runnableLinuxResponseOption.toList ++ runnableMacResponseOption.toList

    if (runnableProcessList.isEmpty) {
      println(s"Could not find process path.")
      throw NativeBuildException(result)
    }
    else {
      var runnableResponse = runnableProcessList.head
      while (runnableResponse.startsWith(" ") || runnableResponse.endsWith(" ")) {
        runnableResponse = runnableResponse.trim
        runnableResponse = runnableResponse.trim
      }
      println(s"Runnable Path after build: $runnableResponse")
//      println(s"Build Output: $result")
      runnableResponse
    }
  }

}
