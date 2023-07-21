import scala.sys.process._
import com.vdurmont.semver4j.Semver

commands += Command.command("ci-deploy-docs")((state: State) => {
  val semVersion = new Semver(version.value)
  if (semVersion.isStable) {
    "sh ./deploy_ghpages.sh".!
    state
  }
  else {
    state
  }
})
