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

  var retries = 0

  def setPort(): Unit = {
    val port = Random.nextInt(10000) + TestAdditions.minPort
    System.setProperty("SERVER_PORT", port.toString)
  }

  while (!serverRunning) {
    try {
      if (!mongoServerStarted) {
        Future.successful {
          MongoTestServer.startMongoDatabase()
          setPort()
          val server = com.quadstingray.mongo.camp.Server.startServer()
          server
        }
        mongoServerStarted = true
      }
      val versionRequest  = InformationApi().version()
      val versionFuture   = TestAdditions.backend.send(versionRequest)
      val versionResponse = Await.result(versionFuture, 1.seconds)
      versionResponse.body.getOrElse(throw new Exception("error"))
      serverRunning = true
    }
    catch {
      case e: Exception =>
        serverRunning = false
        setPort()
        if (retries > 60) {
          throw new Exception(s"could not start server in $retries seconds")
        }
        retries += 1
    }
  }

  def isServerRunning(): Boolean = serverRunning
  def serverBaseUrl: String      = "http://%s:%s".format(com.quadstingray.mongo.camp.Server.interface, com.quadstingray.mongo.camp.Server.port)

}
