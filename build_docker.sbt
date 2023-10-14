import com.vdurmont.semver4j.Semver

import scala.sys.process._

commands += Command.command("ci-docker")((state: State) => {
  val semVersion = new Semver(version.value)
  if (semVersion.isStable) {
    val listOfPlatforms = List("linux/amd64", "linux/arm64/v8", "linux/arm64")

    val containerName = s"mongocamp/mongocamp-server:${version.value}"
    val buildCommand = s"docker buildx build --platform=${listOfPlatforms.mkString(",")} --tag $containerName --push ."
    if (buildCommand.!(ProcessLogger(stout => state.log.info(stout), sterr => state.log.info(sterr))) != 0) {
      throw new Exception(s"Not zero exit code for build base image: ${containerName}")
    }

    // todo: reactivate build if fixed. https://github.com/oracle/graal/issues/7264
//    val containerNameCached = s"mongocamp-server:${version.value}-cached"
//    val buildCommandCached = s"docker buildx build --platform=${listOfPlatforms.mkString(",")} --build-arg MONGOCAMPVERSION=${version.value} --tag ${containerNameCached} -f DockerfileJVMCached --push ."
//    if (buildCommandCached.!(ProcessLogger(stout => state.log.info(stout), sterr => state.log.info(sterr))) != 0) {
//      throw new Exception(s"Not zero exit code for build cached image: ${containerNameCached}")
//    }

    state
  }
  else {
    state
  }
})
