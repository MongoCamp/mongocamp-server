import com.typesafe.sbt.packager.docker.Cmd

enablePlugins(JavaAppPackaging)

enablePlugins(DockerPlugin)

Compile / mainClass := Some("com.quadstingray.mongo.camp.Server")

dockerBaseImage := "openjdk:17-alpine"

maintainer := "QuadStingray, sfxcode"

val mongoCampUser = "mongocamp"

Docker / daemonUser := mongoCampUser

dockerCommands += Cmd("USER", "root")
dockerCommands += Cmd("RUN", "apk add --update --no-cache snappy-dev zlib-dev bash")
dockerCommands += Cmd("USER", mongoCampUser)

dockerExposedPorts := List(8080)
