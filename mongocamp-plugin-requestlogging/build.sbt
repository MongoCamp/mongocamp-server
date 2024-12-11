name := "mongocamp-plugin-requestlogging"

enablePlugins(BuildInfoPlugin)

buildInfoPackage := "dev.mongocamp.server.plugin.requestlogging"

buildInfoOptions += BuildInfoOption.BuildTime

Test / testOptions += Tests.Cleanup(
  (loader: java.lang.ClassLoader) => {
    CleanUpPlugin.cleanup(loader, name.value)
  }
)