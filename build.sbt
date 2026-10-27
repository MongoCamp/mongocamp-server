import common.mongoCampProject
import dev.quadstingray.sbt.json.JsonFile

// sbt 2 applies bare settings of the root build.sbt to every project, so publish / skip has to be scoped to the root project explicitly
lazy val root = Project(id = "mc-server-parent", base = file("."))
  .aggregate(mcCli, mcLibrary, mcServer, mcTest, mcPluginRequestLogging, mcPluginMicrometer)
  .settings(publish / skip := true)

ThisBuild / scalaVersion := "3.9.0"

val json = JsonFile(file("package.json"))

ThisBuild / organization := json.stringValue("organization")


lazy val mcLibrary = mongoCampProject("library")

lazy val mcTest = mongoCampProject("test-server").dependsOn(mcLibrary).enablePlugins(BuildInfoPlugin)

lazy val mcCli = mongoCampProject("cli").dependsOn(mcLibrary).enablePlugins(BuildInfoPlugin)

lazy val mcServer = mongoCampProject("server").enablePlugins(BuildInfoPlugin).dependsOn(mcLibrary, mcTest % Test)

lazy val mcPluginRequestLogging = mongoCampProject("plugin-requestlogging").dependsOn(mcServer, mcTest % Test).enablePlugins(BuildInfoPlugin)

lazy val mcPluginMicrometer = mongoCampProject("plugin-micrometer").dependsOn(mcServer, mcTest % Test).enablePlugins(BuildInfoPlugin)
