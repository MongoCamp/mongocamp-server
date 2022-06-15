enablePlugins(BuildInfoPlugin)

buildInfoPackage := "dev.mongocamp.server"

buildInfoOptions += BuildInfoOption.BuildTime

val MongoCampHomepage = "https://www.mongocamp.dev"

organizationHomepage := Some(url(MongoCampHomepage))

homepage := Some(url("https://mongodb-server.mongocamp.dev"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/MongoCamp/mongodb-server"),
    "scm:https://github.com/MongoCamp/mongodb-server.git"
  )
)

developers := List(
  Developer(
    id = "mongocamp",
    name = "MongoCamp-Team",
    email = "info@mongocamp.dev",
    url = url(MongoCampHomepage)
  ),
  Developer(
    id = "quadstingray",
    name = "QuadStingray",
    email = "simon@mongocamp.dev",
    url = url(MongoCampHomepage)
  ),
  Developer(
    id = "sfxcode",
    name = "Tom",
    email = "tom@mongocamp.dev",
    url = url(MongoCampHomepage)
  )
)

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))