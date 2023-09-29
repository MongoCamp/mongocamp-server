import com.vdurmont.semver4j.Semver

import scala.sys.process.*

commands += Command.command("ci-docker")((state: State) => {
  val semVersion = new Semver(version.value)
  if (semVersion.isStable) {
    val containerName = s"mongocamp-server:${version.value}"

    val buildCommand = s"docker buildx build --platform=linux/amd64,linux/arm64/v8 . --tag $containerName"
    if (buildCommand.!(ProcessLogger(stout => state.log.info(stout), sterr => state.log.info(sterr))) != 0) {
      throw new Exception(s"Not zero exit code for build base image: ${containerName}")
    }

    val pushCommand = s"docker push $containerName"
    if (pushCommand.!(ProcessLogger(stout => state.log.info(stout), sterr => state.log.info(sterr))) != 0) {
      throw new Exception(s"Not zero exit code for push base image; ${containerName}")
    }

//    val containerNameCached = s"mongocamp-server:${version.value}-cached"
//    val buildCommandCached = s"docker build -f DockerfileJVMCached --build-arg MONGOCAMPVERSION=${version.value} --tag ${containerNameCached} ."
//    if (buildCommandCached.!(ProcessLogger(stout => state.log.info(stout), sterr => state.log.info(sterr))) != 0) {
//      throw new Exception(s"Not zero exit code for build cached image: ${containerNameCached}")
//    }
//
//    val pushCommandCached = s"docker push $containerNameCached"
//    if (pushCommandCached.!(ProcessLogger(stout => state.log.info(stout), sterr => state.log.info(sterr))) != 0) {
//      throw new Exception(s"Not zero exit code for push cached image: ${containerNameCached}")
//    }

    state
  }
  else {
    state
  }
})
