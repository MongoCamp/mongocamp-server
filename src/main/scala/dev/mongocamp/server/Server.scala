package dev.mongocamp.server
import dev.mongocamp.server.route._
import dev.mongocamp.server.service.ReflectionService

import scala.concurrent.ExecutionContext

object Server extends App with RestServer {

  implicit val ex: ExecutionContext = ActorHandler.requestExecutionContext

  lazy val listOfRoutePlugins: List[RoutesPlugin] = ReflectionService.instancesForType(classOf[RoutesPlugin])

  override lazy val serverEndpoints = InformationRoutes.routes ++
    AuthRoutes.authEndpoints ++ AdminRoutes.endpoints ++ listOfRoutePlugins.flatMap(
      _.endpoints
    ) ++ IndexRoutes.endpoints

  startServer()

}
