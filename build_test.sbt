Defaults.itSettings

ThisBuild / Test / parallelExecution := false

ThisBuild / IntegrationTest / parallelExecution := false

ThisBuild / Test / scalacOptions ++= Seq("-Yrangepos")