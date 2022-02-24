Defaults.itSettings

Test / parallelExecution := false

IntegrationTest / parallelExecution := false

Test / scalacOptions ++= Seq("-Yrangepos")

// Test
libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test

libraryDependencies += "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "3.4.2" % Test

val sttClientVersion = "2.3.0"
val circeVersion     = "0.14.1"

libraryDependencies += "com.softwaremill.sttp.client" %% "akka-http-backend" % sttClientVersion % Test
libraryDependencies += "com.softwaremill.sttp.client" %% "core"              % sttClientVersion % Test
libraryDependencies += "com.softwaremill.sttp.client" %% "circe"             % sttClientVersion % Test
libraryDependencies += "io.circe"                     %% "circe-core"        % circeVersion     % Test
libraryDependencies += "io.circe"                     %% "circe-generic"     % circeVersion     % Test
libraryDependencies += "io.circe"                     %% "circe-parser"      % circeVersion     % Test
