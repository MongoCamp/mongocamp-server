package dev.mongocamp.server.route

import dev.mongocamp.server.converter.JGroupsConverter
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.model.ClusterInformation
import dev.mongocamp.server.plugin.RoutesPlugin
import io.circe.generic.auto._
import sttp.capabilities
import sttp.capabilities.pekko.PekkoStreams
import sttp.model.{Method, StatusCode}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object SystemRoutes extends BaseRoute with RoutesPlugin {
  private val systemApiBaseEndpoint = adminEndpoint.tag("System").in("system")

  val clusterListRoutes = systemApiBaseEndpoint
    .in("cluster")
    .out(jsonBody[ClusterInformation])
    .summary("Registered Jobs")
    .description("Returns the List of all registered Jobs with full information")
    .method(Method.GET)
    .name("viewCluster")
    .serverLogic(
      _ => _ => viewCluster()
    )

  def viewCluster(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), ClusterInformation]] = {
    Future.successful(Right({
      ClusterInformation(EventSystem.listOfMembers.map(JGroupsConverter.convertAddress), JGroupsConverter.convertAddress(EventSystem.coordinator))
    }))
  }

  override def endpoints: List[ServerEndpoint[PekkoStreams with capabilities.WebSockets, Future]] =
    List(clusterListRoutes)
}
