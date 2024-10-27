name := "mongocamp-plugin-micrometer"

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "dev.mongocamp.server.plugin.micrometer"

buildInfoOptions += BuildInfoOption.BuildTime

libraryDependencies += "dev.mongocamp" %% "micrometer-mongodb" % "1.0.1"
