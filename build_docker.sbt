import com.vdurmont.semver4j.Semver
import scala.sys.process._

commands += Command.command("ci-docker")((state: State) => {
  val semVersion = new Semver(version.value)
  if (semVersion.isStable) {
    val containerName = s"mongocamp-server:${version.value}"

    val buildCommand = s"docker build . --tag $containerName"
    if (buildCommand.!(ProcessLogger(stout => state.log.info(stout), sterr => state.log.info(sterr))) != 0) {
      throw new Exception("Not zero exit code")
    }
    val pushCommand = s"docker push $containerName"
    if (pushCommand.!(ProcessLogger(stout => state.log.info(stout), sterr => state.log.info(sterr))) != 0) {
      throw new Exception("Not zero exit code")
    }

    val containerNameCached = s"mongocamp-server:${version.value}-cached"
    val buildCommandCached = s"docker build DockerfileJVMCached --build-arg \"MONGOCAMPVERSION=${version.value}\" --tag $containerNameCached"
    if (buildCommandCached.!(ProcessLogger(stout => state.log.info(stout), sterr => state.log.info(sterr))) != 0) {
      throw new Exception("Not zero exit code")
    }
    val pushCommandCached = s"docker push $containerNameCached"
    if (pushCommandCached.!(ProcessLogger(stout => state.log.info(stout), sterr => state.log.info(sterr))) != 0) {
      throw new Exception("Not zero exit code")
    }

    state
  }
  else {
    state
  }
})
