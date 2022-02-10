package com.quadstingray.mongo.rest

import com.quadstingray.mongo.rest.routes.InformationRoutes.informationRoutes
import com.quadstingray.mongo.rest.routes._
import com.quadstingray.mongo.rest.server.RestServer

import scala.concurrent.ExecutionContext

object Server extends App with RestServer {

  implicit val ex: ExecutionContext = ExecutionContext.global

  override val serverEndpoints = AuthRoutes.authEndpoints ++ informationRoutes ++ CreateRoutes.createEndpoints ++ ReadRoutes.readEndpoints ++
    UpdateRoutes.updateEndpoints ++ DeleteRoutes.deleteEndpoints ++ IndexRoutes.indexEndpoints

  startServer()

}
