// documentation
lazy val docs = (project in file("docs"))
  .enablePlugins(ParadoxSitePlugin)
  .enablePlugins(ParadoxMaterialThemePlugin)
  .settings(
    name := "docs",
    scalaVersion := "2.13.7",
    publish / skip := true
  )
