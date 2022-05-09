name := "mongocamp-server"

organization := "dev.mongocamp"

scalaVersion := "2.13.8"

libraryDependencies += "dev.mongocamp" %% "mongodb-driver" % "2.4.2"

val TapirVersion = "0.20.1"
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server"   % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"       % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-sttp-client"        % TapirVersion
//libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-redoc-bundle"       % TapirVersion
//libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-docs"       % TapirVersion
//libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-circe-yaml" % TapirVersion

libraryDependencies += "com.github.blemale" %% "scaffeine" % "5.1.2"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.11"

libraryDependencies += "joda-time" % "joda-time" % "2.10.14"

libraryDependencies += "com.github.jwt-scala" %% "jwt-circe" % "9.0.5"

dependencyOverrides += "org.slf4j" % "slf4j-api" % "1.7.30"

dependencyOverrides += "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.0"

libraryDependencies += "org.reflections" % "reflections" % "0.10.2"
