import sbtrelease.ReleasePlugin.autoImport.ReleaseKeys.versions
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.runtimeVersion

import scala.io.Source
import scala.sys.process._
import scala.tools.nsc.io.File

val gitAddAllTask = ReleaseStep(action = st => {
  "git add .".!
  st
})

val gitCommitTask = ReleaseStep(action = st => {
  "git add .".!
  st
})


val generateChangeLog = ReleaseStep(action = st => {
  "conventional-changelog -p angular -i CHANGELOG.md -s -r 0".!
  st
})

val addGithubRelease = ReleaseStep(action = st => {
  "conventional-github-releaser -p angular".!
  st
})

val setToMyNextVersion = ReleaseStep(action = st => {
  setMyVersion(st.get(versions).get._2)
  st
})

val setToMyReleaseVersion = ReleaseStep(action = st => {
  setMyVersion(st.get(versions).get._1)
  st
})

def setMyVersion(version: String) = {
  val packageJsonFile    = File("package.json")
  val source             = Source.fromFile(packageJsonFile.toURI)
  val orgContent         = source.mkString
  val newVersion         = version.replace("-SNAPSHOT", ".snapshot")
  val newVersionString   = "\"version\": \"%s\",".format(newVersion)
  val packageJsonContent = orgContent.replaceAll("\"version\": \"(.*?)\",", newVersionString)
  packageJsonFile.delete()
  packageJsonFile.writeAll(packageJsonContent)
}

releaseCommitMessage := s"ci: set version to ${runtimeVersion.value}"
releaseNextCommitMessage := s"ci: bump to next version ${runtimeVersion.value}"

commands += Command.command("ci-release")((state: State) => {
  val lowerCaseVersion = version.value.toLowerCase
  if (
    (lowerCaseVersion.contains("snapshot") ||
    lowerCaseVersion.contains("beta") ||
    lowerCaseVersion.contains("rc") ||
    lowerCaseVersion.contains("m"))
  ) {
    state
  }
  else {
    Command.process("release with-defaults", state)
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
    setToMyNextVersion,
    commitNextVersion,
    pushChanges,
    addGithubRelease
  )
}
