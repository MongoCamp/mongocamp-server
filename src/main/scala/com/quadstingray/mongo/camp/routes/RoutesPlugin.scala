package com.quadstingray.mongo.camp.routes
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

trait RoutesPlugin extends BaseRoute {

  def routes: List[ServerEndpoint[AkkaStreams with WebSockets, Future]]

}
