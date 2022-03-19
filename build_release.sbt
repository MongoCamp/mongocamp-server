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

val generateChangeLog = ReleaseStep(action = st => {
  st.log.warn("start generating changelog")
  "env DEBUG=\"conventional-github-releaser\"".!!
  val response = "conventional-changelog -p conventionalcommits -i CHANGELOG.md -s -r 0 -n ./changelog/config.js".!!
  st.log.warn("Output of conventional-changelog" + response)
  st
})

val addGithubRelease = ReleaseStep(action = st => {
  st.log.warn("start github release process")
  val response = "conventional-github-releaser -p conventionalcommits -i CHANGELOG.md -s -r 0 -n ./changelog/config.js".!!
  st.log.warn("Output of conventional-github-releaser" + response)
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
  val packageJsonFile    = File("package.json")
  val source             = Source.fromFile(packageJsonFile.toURI)
  val orgContent         = source.mkString
  val newVersion         = version.replace("-SNAPSHOT", ".snapshot")
  val newVersionString   = "\"version\": \"%s\",".format(newVersion)
  val packageJsonContent = orgContent.replaceAll("\"version\": \"(.*?)\",", newVersionString)
  packageJsonFile.delete()
  packageJsonFile.writeAll(packageJsonContent)
  state.log.debug(packageJsonContent)
}

releaseNextCommitMessage := s"ci: update version after release"
releaseCommitMessage := s"ci: prepare release of version ${runtimeVersion.value}"

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
    gitAddAllTask,
    commitNextVersion,
    pushChanges,
    publishArtifacts,
    addGithubRelease
  )
}

publishTo := Some("GitHub Package Registry ".at("https://maven.pkg.github.com/QuadStingray/mongocamp/"))
credentials += Credentials("GitHub Package Registry", "maven.pkg.github.com", System.getenv("GITHUB_USER"), System.getenv("GITHUB_TOKEN"))
