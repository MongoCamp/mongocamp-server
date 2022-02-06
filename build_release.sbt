import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

import scala.sys.process._

val gitAddAllTask = ReleaseStep(action = st => {
  "git add .".!
  st
})

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies, // : ReleaseStep
  inquireVersions,           // : ReleaseStep
  setReleaseVersion,         // : ReleaseStep
  releaseStepCommand("scalafmt"),
  gitAddAllTask,
  commitReleaseVersion, // : ReleaseStep, performs the initial git checks
  tagRelease,           // : ReleaseStep
  setNextVersion,       // : ReleaseStep
  commitNextVersion,    // : ReleaseStep
  pushChanges           // : ReleaseStep, also checks that an upstream branch is properly configured
)


