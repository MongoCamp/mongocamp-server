name := "mongocamp-test-server"

libraryDependencies += "org.scalameta" %% "munit" % "0.7.29"

libraryDependencies += "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "4.6.3"

libraryDependencies += "com.softwaremill.sttp.client3" %% "circe" % "3.8.15"

libraryDependencies += "org.quartz-scheduler" % "quartz" % "2.3.2" % Provided

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "dev.mongocamp.server.server.test"

buildInfoOptions += BuildInfoOption.BuildTime