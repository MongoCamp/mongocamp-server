Defaults.itSettings

Test / parallelExecution := false

IntegrationTest / parallelExecution := false

Test / scalacOptions ++= Seq("-Yrangepos")

libraryDependencies += "dev.mongocamp" %% "mongocamp-test-server" % "0.5.0" % Test