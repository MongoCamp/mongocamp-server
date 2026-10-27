name := "mongocamp-server"

val TapirVersion = "1.14.1"
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-pekko-http-server" % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"      % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-json-circe"        % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-sttp-client"       % TapirVersion
//libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-docs"       % TapirVersion
//libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-circe-yaml" % TapirVersion

val sttClientVersion = "3.11.0"
libraryDependencies += "com.softwaremill.sttp.client3" %% "pekko-http-backend" % sttClientVersion
libraryDependencies += "com.softwaremill.sttp.client3" %% "core"               % sttClientVersion

libraryDependencies += "com.github.jwt-scala" %% "jwt-circe" % "12.0.0"

libraryDependencies += "org.jgroups" % "jgroups" % "5.5.8.Final"

libraryDependencies += "org.quartz-scheduler" % "quartz"   % "2.5.2"
libraryDependencies += "com.zaxxer"           % "HikariCP" % "7.1.0"

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "dev.mongocamp.server"

buildInfoOptions += BuildInfoOption.BuildTime

Test / testOptions += Tests.Cleanup(
  (loader: java.lang.ClassLoader) => CleanUpPlugin.cleanup(loader, name.value)
)
