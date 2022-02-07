import com.typesafe.sbt.packager.docker.Cmd

enablePlugins(JavaAppPackaging)

enablePlugins(DockerPlugin)

Compile / mainClass := Some("com.quadstingray.mongo.rest.Server")

dockerBaseImage := "openjdk:17-alpine"

maintainer := "QuadStingray, sfxcode"

Docker / daemonUser := "mongorest"

dockerCommands += Cmd("USER", "root")
dockerCommands += Cmd("RUN", "apk add --update --no-cache snappy-dev zlib-dev bash")
dockerCommands += Cmd("USER", "mongorest")

dockerExposedPorts := List(8080)
