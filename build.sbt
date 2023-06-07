import dev.quadstingray.sbt.json.JsonFile

val json = JsonFile(file("package.json"))

name := json.stringValue("name")

organization := json.stringValue("organization")

scalaVersion := "2.13.11"

libraryDependencies += "dev.mongocamp" %% "mongodb-driver" % "2.6.3"

val TapirVersion = "1.5.1"
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server"  % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"      % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-json-circe"        % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % TapirVersion
libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-sttp-client"       % TapirVersion
//libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-redoc-bundle"       % TapirVersion
//libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-docs"       % TapirVersion
//libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-asyncapi-circe-yaml" % TapirVersion

val sttClientVersion = "3.8.15"
libraryDependencies += "com.softwaremill.sttp.client3" %% "akka-http-backend" % sttClientVersion
libraryDependencies += "com.softwaremill.sttp.client3" %% "core"              % sttClientVersion

libraryDependencies += "com.github.blemale" %% "scaffeine" % "5.2.1"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.7"

libraryDependencies += "joda-time" % "joda-time" % "2.12.5"

libraryDependencies += "com.github.jwt-scala" %% "jwt-circe" % "9.3.0"

dependencyOverrides += "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.0"

libraryDependencies += "org.reflections" % "reflections" % "0.10.2"

libraryDependencies += "org.quartz-scheduler" % "quartz" % "2.3.2"

libraryDependencies += "io.get-coursier" %% "coursier" % "2.1.4"
