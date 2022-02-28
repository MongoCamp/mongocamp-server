package com.quadstingray.mongo.camp

import com.quadstingray.mongo.camp.routes._
import com.quadstingray.mongo.camp.server.RestServer

import scala.concurrent.ExecutionContext

object Server extends App with RestServer {

  implicit val ex: ExecutionContext = ExecutionContext.global

  def listOfRoutePlugins: List[RoutesPlugin] = List(DatabaseRoutes, CollectionRoutes, DocumentRoutes, ReadRoutes)

  override lazy val serverEndpoints = InformationRoutes.routes ++
    AuthRoutes.authEndpoints ++ AdminRoutes.endpoints ++ listOfRoutePlugins.flatMap(
      _.endpoints
    ) ++ UpdateRoutes.endpoints ++ DeleteRoutes.endpoints ++ IndexRoutes.endpoints

  startServer()

}
