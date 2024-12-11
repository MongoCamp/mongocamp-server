ThisBuild / Test / parallelExecution := false
ThisBuild / parallelExecution := false
Global / parallelExecution  := false
Global / concurrentRestrictions ++= Seq(Tags.limit(Tags.Test, 1), Tags.limit(Tags.Test, 1))

ThisBuild / Test / scalacOptions ++= Seq("-Yrangepos")

Test / testOptions += Tests.Cleanup(
  (loader: java.lang.ClassLoader) => {
    CleanUpPlugin.cleanup(loader, "global")
  }
)
