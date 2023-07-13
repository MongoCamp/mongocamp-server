import com.typesafe.sbt.packager.docker.Cmd

enablePlugins(JavaAppPackaging)

enablePlugins(DockerPlugin)

dockerBaseImage := "openjdk:17-alpine"

maintainer := "QuadStingray, sfxcode"

val mongoCampUser = "mongocamp-server"

dockerRepository := Some("mongocamp")

dockerUpdateLatest := true

Docker / daemonUser := mongoCampUser

dockerCommands += Cmd("USER", "root")
dockerCommands += Cmd("RUN", "apk add --update --no-cache snappy-dev zlib-dev bash;")
dockerCommands += Cmd("RUN", "mkdir -p /opt/mongocamp/plugins; chmod -R 777 /opt/mongocamp/plugins;")
dockerCommands += Cmd("USER", mongoCampUser)

dockerExposedPorts := List(8080)

commands += Command.command("ci-docker")((state: State) => {
  val lowerCaseVersion = version.value.toLowerCase
  if (lowerCaseVersion.contains("snapshot")) {
    state
  }
  else {
    Command.process("docker:publish", state)
    state
  }
})
