package dev.mongocamp.server.route

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

trait RoutesPlugin extends BaseRoute {

  def endpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]]

}
