package dev.mongocamp.server.plugin

import dev.mongocamp.server.route.BaseRoute
import scala.concurrent.Future
import sttp.capabilities.pekko.PekkoStreams
import sttp.capabilities.WebSockets
import sttp.tapir.server.ServerEndpoint

trait RoutesPlugin extends BaseRoute {

  def endpoints: List[ServerEndpoint[PekkoStreams with WebSockets, Future]]

}
