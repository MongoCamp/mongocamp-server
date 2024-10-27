name := "mongocamp-library"

libraryDependencies += "io.get-coursier" %% "coursier" % "2.1.14"

libraryDependencies += "dev.mongocamp" %% "mongodb-driver" % "2.8.0"

libraryDependencies += "io.github.classgraph" % "classgraph" % "4.8.177"

libraryDependencies += ("com.github.blemale" %% "scaffeine" % "5.3.0").exclude("org.scala-lang.modules", "scala-java8-compat_2.12")

val circeVersion = "0.14.10"
libraryDependencies += "io.circe" %% "circe-core"    % circeVersion
libraryDependencies += "io.circe" %% "circe-generic" % circeVersion
libraryDependencies += "io.circe" %% "circe-parser"  % circeVersion

libraryDependencies += "com.softwaremill.sttp.model" %% "core" % "1.7.11"

libraryDependencies += "joda-time" % "joda-time" % "2.13.0"

libraryDependencies += "com.vdurmont" % "semver4j" % "3.1.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.12"

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "dev.mongocamp.server.library"

buildInfoOptions += BuildInfoOption.BuildTime

buildInfoKeys ++= Seq[BuildInfoKey]("organization" -> organization.value, "mainClass" -> "dev.mongocamp.server.Server")
