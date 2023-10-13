package dev.mongocamp.server.plugin

import dev.mongocamp.server.route.BaseRoute
import sttp.capabilities.WebSockets
import sttp.capabilities.pekko.PekkoStreams
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

trait RoutesPlugin extends BaseRoute {

  def endpoints: List[ServerEndpoint[PekkoStreams with WebSockets, Future]]

}
