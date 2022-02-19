name := "mongocamp"

organization := "com.quadstingray"

version := "0.1.0"

scalaVersion := "2.13.8"

libraryDependencies += "com.sfxcode.nosql" %% "simple-mongo" % "2.3.0"

val TapirVersion = "0.19.4"
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

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10"

libraryDependencies += "joda-time" % "joda-time" % "2.10.13"

libraryDependencies += "com.github.jwt-scala" %% "jwt-circe" % "9.0.4"

dependencyOverrides += "org.slf4j" % "slf4j-api" % "1.7.30"

dependencyOverrides += "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.0"
