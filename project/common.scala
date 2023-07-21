import dev.quadstingray.sbt.json.JsonFile
import sbt.*
import sbtrelease.ReleasePlugin.autoImport.*
import sbtrelease.ReleasePlugin.autoImport.ReleaseKeys.versions
import sbtrelease.ReleaseStateTransformations.*

import scala.sys.process.*
object common {

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
    val newVersion = version.replace("-SNAPSHOT", ".snapshot")
    json.updateValue("version", newVersion)
    json.write()
  }

  lazy val releaseSignedArtifactsSettings = Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    setToMyReleaseVersion,
    generateChangeLog,
    releaseStepCommand("scalafmt"),
    gitAddAllTask,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommand("ci-deploy-docs"),
    releaseStepCommand("sonatypeBundleRelease"),
    setToMyNextVersion,
    gitAddAllTask,
    commitNextVersion,
    pushChanges,
    addGithubRelease
  )

  def mongoCampProject(name: String) = {
    val project = Project(s"mongocamp-${name}", file(s"mongocamp-${name}"))
//    project.settings(releaseSignedArtifactsSettings)
    project
  }
}
