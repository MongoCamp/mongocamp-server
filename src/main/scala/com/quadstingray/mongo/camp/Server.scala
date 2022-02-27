package com.quadstingray.mongo.camp

import com.quadstingray.mongo.camp.routes._
import com.quadstingray.mongo.camp.server.RestServer

import scala.concurrent.ExecutionContext

object Server extends App with RestServer {

  implicit val ex: ExecutionContext = ExecutionContext.global

  def listOfRoutePlugins: List[RoutesPlugin] = List(DatabaseRoutes, CollectionRoutes)

  override lazy val serverEndpoints = InformationRoutes.informationRoutes ++
    AuthRoutes.authEndpoints ++ AdminRoutes.adminEndpoints ++ listOfRoutePlugins.flatMap(
      _.routes
    ) ++ CreateRoutes.createEndpoints ++ ReadRoutes.readEndpoints ++
    UpdateRoutes.updateEndpoints ++ DeleteRoutes.deleteEndpoints ++ IndexRoutes.indexEndpoints

  startServer()

}
