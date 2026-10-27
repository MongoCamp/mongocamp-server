ThisBuild / Test / parallelExecution := false
Global / parallelExecution           := false
Global / concurrentRestrictions += Tags.limit(Tags.Test, 1)


Test / testOptions += Tests.Cleanup(
  (loader: java.lang.ClassLoader) => CleanUpPlugin.cleanup(loader, "global")
)
