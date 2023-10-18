name := "mongocamp-server"

val TapirVersion = "1.8.2"
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-pekko-http-server" % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"      % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-json-circe"        % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-sttp-client"       % TapirVersion
//libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-docs"       % TapirVersion
//libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-circe-yaml" % TapirVersion

val sttClientVersion = "3.9.0"
libraryDependencies += "com.softwaremill.sttp.client3" %% "pekko-http-backend" % sttClientVersion
libraryDependencies += "com.softwaremill.sttp.client3" %% "core"               % sttClientVersion

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.11"

libraryDependencies += "com.github.jwt-scala" %% "jwt-circe" % "9.4.4"

libraryDependencies += "org.quartz-scheduler" % "quartz" % "2.3.2"

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "dev.mongocamp.server"

buildInfoOptions += BuildInfoOption.BuildTime
