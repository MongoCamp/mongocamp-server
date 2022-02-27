package com.quadstingray.mongo.camp

import com.quadstingray.mongo.camp.routes.InformationRoutes.informationRoutes
import com.quadstingray.mongo.camp.routes._
import com.quadstingray.mongo.camp.server.RestServer

import scala.concurrent.ExecutionContext

object Server extends App with RestServer {

  implicit val ex: ExecutionContext = ExecutionContext.global

  def listOfRoutePlugins: List[RoutesPlugin] = List(DatabaseRoutes)

  override lazy val serverEndpoints =
    AuthRoutes.authEndpoints ++ AdminRoutes.adminEndpoints ++ listOfRoutePlugins.flatMap(
      _.routes
    ) ++ informationRoutes ++ CreateRoutes.createEndpoints ++ ReadRoutes.readEndpoints ++
      UpdateRoutes.updateEndpoints ++ DeleteRoutes.deleteEndpoints ++ IndexRoutes.indexEndpoints

  startServer()

}
