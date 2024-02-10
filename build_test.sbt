ThisBuild / Test / parallelExecution := false
Global / parallelExecution  := false
Global / concurrentRestrictions += Tags.limit(Tags.Test, 1)

ThisBuild / Test / scalacOptions ++= Seq("-Yrangepos")