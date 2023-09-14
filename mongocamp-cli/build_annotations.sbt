lazy val processAnnotations = taskKey[Unit]("Process annotations")

processAnnotations := {
  val log = streams.value.log

  log.info("Processing annotations ...")

  val classpath = ((Compile / products).value ++ ((Compile / dependencyClasspath).value.files)).mkString(":")
  val destinationDirectory = (Compile / classDirectory).value
  val processor = "picocli.codegen.aot.graalvm.processor.NativeImageConfigGeneratorProcessor"

  val classesToProcess = recursiveFileSearch(destinationDirectory).filter(_.toString.contains(".class"))
    .map(
      filename => filename.toString.replace(s"$destinationDirectory/", "").replace(".class", "").replace('/', '.')
    )

  val command = s"javac -cp $classpath -proc:only -processor $processor -XprintRounds -d $destinationDirectory ${classesToProcess.mkString(" ")}"

//  println(command)
  failIfNonZeroExitStatus(command, "Failed to process annotations.", log)

  log.info("Done processing annotations.")
}

def recursiveFileSearch(file: File): Array[File] = {
  val files = if (file.isFile) {
    Array(file)
  }
  else {
    file.listFiles().flatMap(recursiveFileSearch)
  }
  //  println(files.map(_.toString).mkString("Array(", ", ", ")"))
  files
}

def failIfNonZeroExitStatus(command: String, message: => String, log: Logger) = {
  import scala.sys.process.*
  val result = command !

  if (result != 0) {
    log.error(message)
    sys.error("Failed running command: " + command)
  }
}

Compile / packageBin := ((Compile / packageBin).dependsOn(Compile / processAnnotations)).value
