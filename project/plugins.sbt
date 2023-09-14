addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.4.1")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")

addDependencyTreePlugin

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.1")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.21")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.1.1")

addSbtPlugin("dev.quadstingray" %% "sbt-json" % "0.6.5")

dependencyOverrides += "org.scala-lang.modules" %% "scala-xml" % "1.2.0"

libraryDependencies += ("com.vdurmont" % "semver4j" % "3.1.0")
