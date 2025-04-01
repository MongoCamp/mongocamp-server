name := "mongocamp-test-server"

libraryDependencies += "org.testcontainers" % "testcontainers" % "1.20.6"

libraryDependencies += "org.scalameta" %% "munit" % "1.1.0"

libraryDependencies += "com.softwaremill.sttp.client3" %% "circe" % "3.10.3"

libraryDependencies += "org.quartz-scheduler" % "quartz" % "2.5.0" % Provided

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "dev.mongocamp.server.server.test"

buildInfoOptions += BuildInfoOption.BuildTime

Test / testOptions += Tests.Cleanup(
  (loader: java.lang.ClassLoader) => {
    CleanUpPlugin.cleanup(loader, name.value)
  }
)