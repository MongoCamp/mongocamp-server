import sbt.*
object common {
  def mongoCampProject(name: String) = {
    val project = Project(s"mongocamp-${name}", file(s"mongocamp-${name}"))
    project
  }
}
