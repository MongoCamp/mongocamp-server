// Publishing to the Sonatype Central Portal is built into sbt (1.12.15+), the legacy OSSRH endpoints were sunset on 2025-06-30.
// Releases are staged by publishSigned and published by sonaRelease. The credentials for central.sonatype.com (Central Portal user token)
// are read from the environment variables SONATYPE_USER and SONATYPE_PASSWORD, which the release workflow sets.
// https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html
ThisBuild / publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}

credentials += Credentials("central.sonatype.com", "central.sonatype.com", System.getenv("SONATYPE_USER"), System.getenv("SONATYPE_PASSWORD"))

ThisBuild / publishMavenStyle := true

ThisBuild / pomIncludeRepository := { _ => false }

Global / useGpgPinentry := true

packageOptions += {
  Package.ManifestAttributes(
    "Created-By"               -> "Simple Build Tool",
    "Built-By"                 -> "mongocamp",
    "Build-Jdk"                -> System.getProperty("java.version"),
    "Specification-Title"      -> name.value,
    "Specification-Version"    -> version.value,
    "Specification-Vendor"     -> organization.value,
    "Implementation-Title"     -> name.value,
    "Implementation-Version"   -> version.value,
    "Implementation-Vendor-Id" -> organization.value,
    "Implementation-Vendor"    -> organization.value
  )
}
