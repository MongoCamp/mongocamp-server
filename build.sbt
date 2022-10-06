name := "mongocamp-server"

organization := "dev.mongocamp"

scalaVersion := "2.13.9"

libraryDependencies += "dev.mongocamp" %% "mongodb-driver" % "2.5.0"

val TapirVersion = "1.1.2"
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server"   % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"       % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-sttp-client"        % TapirVersion
//libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-redoc-bundle"       % TapirVersion
//libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-docs"       % TapirVersion
//libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-circe-yaml" % TapirVersion

val sttClientVersion = "3.8.2"
libraryDependencies += "com.softwaremill.sttp.client3" %% "akka-http-backend" % sttClientVersion
libraryDependencies += "com.softwaremill.sttp.client3" %% "core"              % sttClientVersion

libraryDependencies += "com.github.blemale" %% "scaffeine" % "5.2.1"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.3"

libraryDependencies += "joda-time" % "joda-time" % "2.11.2"

libraryDependencies += "com.github.jwt-scala" %% "jwt-circe" % "9.1.1"

dependencyOverrides += "org.slf4j" % "slf4j-api" % "1.7.30"

dependencyOverrides += "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.0"

libraryDependencies += "org.reflections" % "reflections" % "0.10.2"

libraryDependencies += "io.micrometer" % "micrometer-core" % "1.9.4"

libraryDependencies += "org.quartz-scheduler" % "quartz" % "2.3.2"
