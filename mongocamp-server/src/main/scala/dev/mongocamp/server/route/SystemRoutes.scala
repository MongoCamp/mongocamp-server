package dev.mongocamp.server.route

import dev.mongocamp.server.converter.JGroupsConverter
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.model.Capabilities
import dev.mongocamp.server.model.ClusterInformation
import dev.mongocamp.server.model.SystemInfo
import dev.mongocamp.server.model.SystemStats
import dev.mongocamp.server.plugin.RoutesPlugin
import dev.mongocamp.server.service.CapabilitiesService
import dev.mongocamp.server.service.MongoDbInfoService
import dev.mongocamp.server.service.SystemStatsService
import io.circe.generic.auto._
import scala.concurrent.Future
import sttp.capabilities
import sttp.capabilities.pekko.PekkoStreams
import sttp.model.Method
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

object SystemRoutes extends BaseRoute with RoutesPlugin {
  private val systemApiBaseEndpoint = adminEndpoint.tag("System").in("system")
  // for every logged in user: a client needs the capabilities for every user
  private val systemApiUserEndpoint = securedEndpoint.tag("System").in("system")

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
    Future.successful(Right {
      ClusterInformation(EventSystem.listOfMembers.map(JGroupsConverter.convertAddress), JGroupsConverter.convertAddress(EventSystem.coordinator))
    })
  }

  val systemInfoRoute = systemApiBaseEndpoint
    .in("info")
    .out(jsonBody[SystemInfo])
    .summary("System Information")
    .description("Information about the system the server is connected to: the version of the MongoDB server")
    .method(Method.GET)
    .name("systemInfo")
    .serverLogic(
      _ => _ => systemInfo()
    )

  def systemInfo(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), SystemInfo]] =
    Future.successful(Right(MongoDbInfoService.systemInfo()))

  val systemStatsRoute = systemApiBaseEndpoint
    .in("stats")
    .out(jsonBody[SystemStats])
    .summary("System Statistics")
    .description(
      "The numbers of the database server for a server overview: uptime, connections, operation counters (totals since the start of the database server) and the state of the replica set. A database user without the role clusterMonitor gives partial true and no uptime, connections and counters; a server that is not part of a replica set has no replicaSet. No host names or addresses."
    )
    .method(Method.GET)
    .name("systemStats")
    .serverLogic(
      _ => _ => systemStats()
    )

  def systemStats(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), SystemStats]] =
    Future.successful(Right(SystemStatsService.systemStats()))

  val systemCapabilitiesRoute = systemApiUserEndpoint
    .in("capabilities")
    .out(jsonBody[Capabilities])
    .summary("System Capabilities")
    .description(
      "What this server can do, for every logged in user: the names of the features, the major version of MongoDB, the limits (size of an import, status of a request without the grant) and the operators that an aggregation refuses. A server that has no such route is an older version."
    )
    .method(Method.GET)
    .name("systemCapabilities")
    .serverLogic(
      _ => _ => systemCapabilities()
    )

  def systemCapabilities(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Capabilities]] =
    Future.successful(Right(CapabilitiesService.capabilities()))

  override def endpoints: List[ServerEndpoint[PekkoStreams with capabilities.WebSockets, Future]] =
    List(clusterListRoutes, systemInfoRoute, systemStatsRoute, systemCapabilitiesRoute)
}
