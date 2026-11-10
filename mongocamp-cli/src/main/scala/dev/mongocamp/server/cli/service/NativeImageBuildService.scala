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
    val javaBin = new java.io.File(JvmService.javaHome, "bin")
    // GraalVM for JDK 21+ ships native-image and has no gu (graal updater), older GraalVM distributions install it with gu
    if (!new java.io.File(javaBin, "native-image").exists()) {
      val gu = new java.io.File(javaBin, "gu")
      if (gu.exists()) {
        ProcessExecutorService.executeToString(s"$gu install native-image")
      }
      else {
        throw new RuntimeException(
          s"native-image is not part of the JVM in ${JvmService.javaHome}, use a GraalVM distribution that includes it (GraalVM for JDK 21+)"
        )
      }
    }
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

    // the artifact list ("Build artifacts:" or "Produced artifacts:") can hold other files (e.g. a jdk_library) before the executable
    val runnableRegex       = "(?m)^[ \\t]*(\\S.*?) \\(executable\\)[ \\t]*$".r
    val runnableProcessList = runnableRegex.findAllMatchIn(result).toList.lastOption.map(_.group(1)).toList

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
