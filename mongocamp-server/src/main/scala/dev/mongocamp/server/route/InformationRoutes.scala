package dev.mongocamp.server.route

import dev.mongocamp.server.BuildInfo
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.model.Version
import dev.mongocamp.server.plugin.RoutesPlugin
import io.circe.generic.auto._
import org.joda.time.DateTime
import sttp.capabilities
import sttp.capabilities.pekko.PekkoStreams
import sttp.model.{Method, StatusCode}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object InformationRoutes extends BaseRoute with RoutesPlugin{

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
  override def endpoints: List[ServerEndpoint[PekkoStreams with capabilities.WebSockets, Future]] = List(version)
}
