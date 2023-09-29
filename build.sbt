import common.mongoCampProject
import dev.quadstingray.sbt.json.JsonFile

lazy val root = Project(id = "mc-server-parent", base = file(".")).aggregate(mcCli, mcLibrary, mcServer)

ThisBuild / scalaVersion := "2.13.11"

val json = JsonFile(file("package.json"))

ThisBuild / organization := json.stringValue("organization")

publish / skip := true

lazy val mcLibrary = mongoCampProject("library")

lazy val mcTest = mongoCampProject("test-server").dependsOn(mcLibrary).enablePlugins(BuildInfoPlugin)

lazy val mcCli = mongoCampProject("cli").dependsOn(mcLibrary).enablePlugins(BuildInfoPlugin)

lazy val mcServer = mongoCampProject("server").enablePlugins(BuildInfoPlugin).dependsOn(mcLibrary, mcTest % Test)
