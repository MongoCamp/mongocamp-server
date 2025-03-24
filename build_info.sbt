import dev.quadstingray.sbt.json.JsonFile

val json = JsonFile(file("package.json"))

val MongoCampHomepage = "https://www.mongocamp.dev"

ThisBuild / organizationHomepage := Some(url(MongoCampHomepage))

ThisBuild / homepage := Some(url(json.stringValue("homepage")))

ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/MongoCamp/mongodb-server"), "scm:https://github.com/MongoCamp/mongodb-server.git"))

ThisBuild / developers := List(
  Developer(id = "mongocamp", name = "MongoCamp-Team", email = "info@mongocamp.dev", url = url(MongoCampHomepage)),
  Developer(id = "quadstingray", name = "QuadStingray", email = "simon@mongocamp.dev", url = url(MongoCampHomepage)),
  Developer(id = "sfxcode", name = "Tom", email = "tom@mongocamp.dev", url = url(MongoCampHomepage))
)

ThisBuild / licenses += (json.stringValue("license"), url("https://github.com/MongoCamp/mongocamp-server/blob/main/LICENSE"))
