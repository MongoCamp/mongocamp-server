import sbtrelease.ReleasePlugin.autoImport.ReleaseKeys.versions
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.runtimeVersion
import dev.quadstingray.sbt.json.JsonFile
import com.vdurmont.semver4j.Semver

import scala.sys.process._

val gitAddAllTask = ReleaseStep(action = st => {
  "git add .".!
  st
})

val generateChangeLog = ReleaseStep(action = st => {
  st.log.warn("start generating changelog")
  val response = "conventional-changelog -p conventionalcommits -i CHANGELOG.md -s -r 0 -n ./changelog/config.js".!!
  st.log.warn("Output of conventional-changelog" + response)
  st
})

val addGithubRelease = ReleaseStep(action = st => {
  st.log.warn("start github release process")
  var response = ""
  try response = "conventional-github-releaser -p conventionalcommits -r 3 -n ./changelog/config.js".!!
  catch {
    case e: Exception =>
      st.log.warn("Catched Exception on generate release notes: " + e.getMessage)
  }
  st.log.warn("Output of conventional-github-releaser: " + response)
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
    generateChangeLog,
    releaseStepCommand("scalafmt"),
    gitAddAllTask,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommand("mongocamp-library/publishSigned"),
    releaseStepCommand("mongocamp-server/publishSigned"),
    releaseStepCommand("mongocamp-test-server/publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
//    releaseStepCommand("mongocamp-library/sonatypeBundleRelease"),
//    releaseStepCommand("mongocamp-server/sonatypeBundleRelease"),
//    releaseStepCommand("mongocamp-test-server/sonatypeBundleRelease"),
    releaseStepCommand("ci-docker"),
    releaseStepCommand("ci-deploy-docs"),
    setToMyNextVersion,
    gitAddAllTask,
    commitNextVersion,
    pushChanges,
    addGithubRelease
  )
}