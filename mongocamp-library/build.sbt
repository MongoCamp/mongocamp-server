name := "mongocamp-library"

libraryDependencies += "io.get-coursier" %% "coursier" % "2.1.26"

libraryDependencies += "dev.mongocamp" %% "mongodb-driver" % "3.3.7"

libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.20"

libraryDependencies += "io.github.classgraph" % "classgraph" % "4.8.199"

libraryDependencies += ("com.github.blemale" %% "scaffeine" % "5.3.0")

val circeVersion = "0.14.17"
libraryDependencies += "io.circe" %% "circe-core"    % circeVersion
libraryDependencies += "io.circe" %% "circe-generic" % circeVersion
libraryDependencies += "io.circe" %% "circe-parser"  % circeVersion

libraryDependencies += "com.softwaremill.sttp.model" %% "core" % "1.9.0"

libraryDependencies += "joda-time" % "joda-time" % "2.15.0"

libraryDependencies += "org.semver4j" % "semver4j" % "6.0.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.6.6"

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "dev.mongocamp.server.library"

buildInfoOptions += BuildInfoOption.BuildTime

buildInfoKeys ++= Seq[BuildInfoKey]("organization" -> organization.value, "mainClass" -> "dev.mongocamp.server.Server")

libraryDependencies += "org.apache.pekko" %% "pekko-actor" % "1.7.1" % Provided

Test / testOptions += Tests.Cleanup(
  (loader: java.lang.ClassLoader) => {
    CleanUpPlugin.cleanup(loader, name.value)
  }
)