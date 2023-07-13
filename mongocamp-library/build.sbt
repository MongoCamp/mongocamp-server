name := "mongocamp-library"

libraryDependencies += "io.get-coursier" %% "coursier" % "2.1.5"

libraryDependencies += "dev.mongocamp" %% "mongodb-driver" % "2.6.3"

libraryDependencies += "org.reflections" % "reflections" % "0.10.2"

libraryDependencies += "com.github.blemale" %% "scaffeine" % "5.2.1" exclude("org.scala-lang.modules", "scala-java8-compat_2.12")

val circeVersion     = "0.14.5"
libraryDependencies += "io.circe"                      %% "circe-core"        % circeVersion
libraryDependencies += "io.circe"                      %% "circe-generic"     % circeVersion
libraryDependencies += "io.circe"                      %% "circe-parser"      % circeVersion

libraryDependencies += "com.softwaremill.sttp.model" %% "core" % "1.7.0"

libraryDependencies += "joda-time" % "joda-time" % "2.12.5"

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "dev.mongocamp.server.library"

buildInfoOptions += BuildInfoOption.BuildTime