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
  "conventional-changelog -p angular -i CHANGELOG.md -s -r 0".!
  st
})

val addGithubRelease = ReleaseStep(action = st => {
  "conventional-github-releaser -p angular".!
  st
})

val setToMyNextVersion = ReleaseStep(action = st => {
  val packageJsonFile    = File("package.json")
  val source             = Source.fromFile(packageJsonFile.toURI)
  val orgContent         = source.mkString
  val newVersionString   = "\"version\": \"%s\",".format(st.get(versions).get._2)
  val packageJsonContent = orgContent.replaceAll("\"version\": \"(.*?)\",", newVersionString)
  packageJsonFile.delete()
  packageJsonFile.writeAll(packageJsonContent)
  st
})

releaseNextCommitMessage := s"ci: bump next version to ${runtimeVersion.value}"

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  setReleaseVersion,
  releaseStepCommand("scalafmt"),
  gitAddAllTask,
  commitReleaseVersion,
  tagRelease,
  addGithubRelease,
  setToMyNextVersion,
  gitAddAllTask,
  pushChanges
)
