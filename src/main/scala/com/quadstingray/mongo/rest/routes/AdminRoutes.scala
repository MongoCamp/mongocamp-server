package com.quadstingray.mongo.rest.routes

import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object AdminRoutes extends BaseRoute {
  private val adminBase = adminEndpoint.in("admin").tag("Admin")

  lazy val adminEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = {
    if (globalConfigString("mongorest.auth.handler").equalsIgnoreCase("mongo")) {
      List()
    }
    else {
      List()
    }
  }

}
