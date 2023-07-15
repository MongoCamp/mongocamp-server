enablePlugins(GraalVMNativeImagePlugin)

val PicoliCliVersion = "4.7.4"
libraryDependencies += "info.picocli" % "picocli"         % PicoliCliVersion
libraryDependencies += "info.picocli" % "picocli-codegen" % PicoliCliVersion % "provided"

libraryDependencies += "com.github.lukfor" % "magic-progress" % "0.3.2"

libraryDependencies += "com.typesafe.scala-logging"   %% "scala-logging" % "3.9.5"
libraryDependencies += "ch.qos.logback"                % "logback-classic" % "1.4.8"

publish / skip := true

graalVMNativeImageOptions ++= Seq(
  "--no-fallback",
  "--verbose",
  "-H:+ReportExceptionStackTraces",
  "-H:+ReportUnsupportedElementsAtRuntime",
  "-H:+ReportExceptionStackTraces",
  "--allow-incomplete-classpath",
  "--report-unsupported-elements-at-runtime"
)
