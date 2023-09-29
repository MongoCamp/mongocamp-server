enablePlugins(GraalVMNativeImagePlugin)

val PicoliCliVersion = "4.7.5"
libraryDependencies += "info.picocli" % "picocli"         % PicoliCliVersion
libraryDependencies += "info.picocli" % "picocli-codegen" % PicoliCliVersion % "provided"

libraryDependencies += "com.github.lukfor" % "magic-progress" % "0.3.2"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5"
libraryDependencies += "ch.qos.logback"              % "logback-classic" % "1.4.11"

publish / skip := true

libraryDependencies += "io.get-coursier" %% "coursier-jvm" % "2.1.7"

graalVMNativeImageOptions ++= Seq(
  "--no-fallback",
  "--verbose",
  "-H:+ReportExceptionStackTraces",
  "-H:+ReportUnsupportedElementsAtRuntime",
  "-H:+ReportExceptionStackTraces",
  "--report-unsupported-elements-at-runtime",
  "--trace-object-instantiation=java.io.File,java.util.jar.JarFile",
  "-J-Xmx16g"
)

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "dev.mongocamp.server.cli"

buildInfoOptions += BuildInfoOption.BuildTime

buildInfoKeys ++= Seq[BuildInfoKey]("organization" -> organization.value, "buildJavaVersion" -> System.getProperty("java.version"))
