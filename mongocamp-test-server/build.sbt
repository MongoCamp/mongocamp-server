name := "mongocamp-test-server"

libraryDependencies += "org.testcontainers" % "testcontainers" % "1.20.4"

libraryDependencies += "org.scalameta" %% "munit" % "1.0.2"

libraryDependencies += "com.softwaremill.sttp.client3" %% "circe" % "3.10.1"

libraryDependencies += "org.quartz-scheduler" % "quartz" % "2.5.0" % Provided

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "dev.mongocamp.server.server.test"

buildInfoOptions += BuildInfoOption.BuildTime