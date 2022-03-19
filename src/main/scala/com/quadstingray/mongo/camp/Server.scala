package com.quadstingray.mongo.camp

import com.quadstingray.mongo.camp.routes._
import com.quadstingray.mongo.camp.server.RestServer
import com.quadstingray.mongo.camp.service.ReflectionService

import scala.concurrent.ExecutionContext

object Server extends App with RestServer {

  implicit val ex: ExecutionContext = ExecutionContext.global

  lazy val listOfRoutePlugins: List[RoutesPlugin] = ReflectionService.instancesForType(classOf[RoutesPlugin])

  override lazy val serverEndpoints = InformationRoutes.routes ++
    AuthRoutes.authEndpoints ++ AdminRoutes.endpoints ++ listOfRoutePlugins.flatMap(
      _.endpoints
    ) ++ IndexRoutes.endpoints

  startServer()

}
