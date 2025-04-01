import com.vdurmont.semver4j.Semver
import scala.sys.process.*

commands += Command.command("ci-docker")(
  (state: State) => {
    val semVersion = new Semver(version.value)
    if (semVersion.isStable) {
//      val listOfPlatforms = List("linux/amd64", "linux/arm64/v8", "linux/arm64")
      val listOfPlatforms = List("linux/amd64")
      val listOfTags      = List("latest", version.value, s"${semVersion.getMajor}.${semVersion.getMinor}", s"${semVersion.getMajor}")
      val containerNameList = listOfTags
        .map(
          tag => s"mongocamp/mongocamp-server:$tag"
        )
        .mkString(" --tag ")
      val containerName = s"mongocamp/mongocamp-server:${version.value}"
      val buildCommand  = s"docker buildx build --platform=${listOfPlatforms.mkString(",")} --tag $containerNameList --push ."
      state.log.error(s"BuildCommand: $buildCommand")
      if (buildCommand.!(ProcessLogger(stout => state.log.info(stout), sterr => state.log.info(sterr))) != 0) {
        throw new Exception(s"Not zero exit code for build base images: ${containerNameList}")
      }

      val containerNameCached = s"mongocamp-server:${version.value}-cached"
      val buildCommandCached =
        s"docker buildx build --platform=${listOfPlatforms.mkString(",")} --build-arg MONGOCAMPVERSION=${version.value} --tag ${containerNameCached} -f DockerfileJVMCached --push ."
      if (buildCommandCached.!(ProcessLogger(stout => state.log.info(stout), sterr => state.log.info(sterr))) != 0) {
        throw new Exception(s"Not zero exit code for build cached image: ${containerNameCached}")
      }

      state
    }
    else {
      state
    }
  }
)
