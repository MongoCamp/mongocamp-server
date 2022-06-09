Defaults.itSettings

Test / parallelExecution := false

IntegrationTest / parallelExecution := false

Test / scalacOptions ++= Seq("-Yrangepos")

// Test
libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test

libraryDependencies += "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "3.4.6" % Test

val sttClientVersion = "3.6.2"
val circeVersion     = "0.14.2"

libraryDependencies += "com.softwaremill.sttp.client3" %% "akka-http-backend" % sttClientVersion % Test
libraryDependencies += "com.softwaremill.sttp.client3" %% "core"              % sttClientVersion % Test
libraryDependencies += "com.softwaremill.sttp.client3" %% "circe"             % sttClientVersion % Test
libraryDependencies += "io.circe"                      %% "circe-core"        % circeVersion     % Test
libraryDependencies += "io.circe"                      %% "circe-generic"     % circeVersion     % Test
libraryDependencies += "io.circe"                      %% "circe-parser"      % circeVersion     % Test
