import com.vdurmont.semver4j.Semver
import dev.quadstingray.sbt.json.JsonFile
import sbtrelease.ReleasePlugin.autoImport.ReleaseKeys.versions
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations.*
import sbtrelease.ReleasePlugin.runtimeVersion

import scala.sys.process.*

val gitAddAllTask = ReleaseStep(action = st => {
  "git add .".!
  st
})

val setToMyNextVersion = ReleaseStep(action = st => {
  setMyVersion(st.get(versions).get._2, st)
  st
})

val setToMyReleaseVersion = ReleaseStep(action = st => {
  setMyVersion(st.get(versions).get._1, st)
  st
})

def setMyVersion(version: String, state: State): Unit = {
  state.log.warn(s"Set Version in package.json  to $version")
  val json = JsonFile(file("package.json"))
  val newVersion         = version.replace("-SNAPSHOT", ".snapshot")
  json.updateValue("version", newVersion)
  json.write()
}

releaseNextCommitMessage := s"ci: update version after release"
releaseCommitMessage     := s"ci: prepare release of version ${runtimeVersion.value}"

commands += Command.command("ci-release")((state: State) => {
  val semVersion = new Semver(version.value)
  if (semVersion.isStable) {
    Command.process("release with-defaults", state)
  }
  else {
    state
  }
})

releaseProcess := {
  Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    setToMyReleaseVersion,
    releaseStepCommand("scalafmt"),
    gitAddAllTask,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommand("ci-docker"),
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
    releaseStepCommand("ci-deploy-docs"),
    setToMyNextVersion,
    gitAddAllTask,
    commitNextVersion,
    pushChanges
  )
}