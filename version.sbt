import scala.io.Source
import scala.tools.nsc.io.File

ThisBuild / version := {
  val packageJsonFile   = File("package.json")
  val source            = Source.fromFile(packageJsonFile.toURI)
  val versionPattern    = "\"version\": \"(.*?)\",".r
  val versionPartString = versionPattern.findFirstIn(source.mkString).get.replace("\"version\": \"", "").replace("\",", "")
  versionPartString
}
