import com.vdurmont.semver4j.Semver
import scala.sys.process._

commands += Command.command("ci-docker")((state: State) => {
  val semVersion = new Semver(version.value)
  if (semVersion.isStable) {
    val containerName = s"mongocamp-server:${version.value}"
    val buildCommand = s"docker build . --tag $containerName"
    buildCommand.!(ProcessLogger(stout => state.log.info(stout), sterr => state.log.info(sterr)))
    val pushCommand = s"docker push $containerName"
    pushCommand.!(ProcessLogger(stout => state.log.info(stout), sterr => state.log.info(sterr)))
    state
  }
  else {
    state
  }
})
