package dev.mongocamp.server.route

import dev.mongocamp.server.BuildInfo
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.model.Version
import io.circe.generic.auto._
import org.joda.time.DateTime
import sttp.model.{Method, StatusCode}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

import scala.concurrent.Future

object InformationRoutes extends BaseRoute {

  val version = baseEndpoint
    .in("version")
    .out(jsonBody[Version])
    .summary("Version Information")
    .description("Version Info of the MongoCamp API")
    .tag("Information")
    .method(Method.GET)
    .name("version")
    .serverLogic(_ => createVersion())

  def createVersion(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Version]] = {
    Future.successful(Right(Version(BuildInfo.name, BuildInfo.version, new DateTime(BuildInfo.builtAtMillis).toDate)))
  }

  val routes = List(version)
}
