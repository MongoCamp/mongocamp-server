addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")

addSbtPlugin("com.github.sbt" % "sbt-release" % "1.5.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.2")

addDependencyTreePlugin

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.13.0")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.2")

addSbtPlugin("dev.quadstingray" % "sbt-json" % "0.8.1")

libraryDependencies += ("com.vdurmont" % "semver4j" % "3.1.0")

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "7.8.0.202609011348-r"
