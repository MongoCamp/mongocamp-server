import com.typesafe.sbt.packager.docker.Cmd

import scala.sys.process._

enablePlugins(JavaAppPackaging)

enablePlugins(DockerPlugin)

dockerBaseImage := "openjdk:17-alpine"

maintainer := "QuadStingray, sfxcode"

val mongoCampUser = "mongocamp-server"

dockerRepository := Some("ghcr.io/mongocamp")

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
    val dockerHubRepository = "mongocamp"
    val originalContainerName = s"${dockerRepository.value.get}/${name.value}:${version.value}"
    val newContainerName = originalContainerName.replace(dockerRepository.value.get, dockerHubRepository)

    val dockerTagCommand = s"docker tag $originalContainerName $newContainerName"
    val tagResponse = dockerTagCommand.!!

    val dockerPushCommand = s"docker push $newContainerName"
    val pushResponse = dockerPushCommand.!!
    state.log.warn("Tag Container <pushResponse>" + pushResponse + "</pushResponse>")

    state
  }
})
