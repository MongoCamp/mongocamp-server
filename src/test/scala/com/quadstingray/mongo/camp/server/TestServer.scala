package com.quadstingray.mongo.camp.server

import com.quadstingray.mongo.camp.client.api.InformationApi
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContext, ExecutionContextExecutor, Future }
import scala.util.Random

object TestServer extends LazyLogging {
  implicit val ex: ExecutionContextExecutor = ExecutionContext.global

  private var serverRunning      = false
  private var mongoServerStarted = false

  System.setProperty("CONNECTION_HOST", "localhost")
  System.setProperty("CONNECTION_DATABASE", "test")

  while (!serverRunning) {
    try {
      if (!mongoServerStarted) {
        Future.successful {
          MongoTestServer.startMongoDatabase()
          val port = Random.nextInt(10000).toString
          System.setProperty("SERVER_PORT", port)
          val server = com.quadstingray.mongo.camp.Server.startServer()
          server
        }
        mongoServerStarted = true
      }
      val versionRequest  = InformationApi().version()
      val versionFuture   = TestAdditions.backend.send(versionRequest)
      val versionResponse = Await.result(versionFuture, 1.seconds)
      val version         = versionResponse.body.getOrElse(throw new Exception("error"))
      version
      serverRunning = true
    }
    catch {
      case e: Exception =>
        serverRunning = false
    }
  }

  def isServerRunning(): Boolean = serverRunning
  def serverBaseUrl: String      = "http://%s:%s".format(com.quadstingray.mongo.camp.Server.interface, com.quadstingray.mongo.camp.Server.port)

}
