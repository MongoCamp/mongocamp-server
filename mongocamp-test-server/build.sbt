name := "mongocamp-test-server"

libraryDependencies += "org.testcontainers" % "testcontainers" % "2.0.5"

libraryDependencies += "org.scalameta" %% "munit" % "1.3.6"

libraryDependencies += "com.softwaremill.sttp.client3" %% "circe" % "3.11.0"

libraryDependencies += "org.quartz-scheduler" % "quartz" % "2.5.2" % Provided

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "dev.mongocamp.server.server.test"

buildInfoOptions += BuildInfoOption.BuildTime

Test / testOptions += Tests.Cleanup(
  (loader: java.lang.ClassLoader) => {
    CleanUpPlugin.cleanup(loader, name.value)
  }
)