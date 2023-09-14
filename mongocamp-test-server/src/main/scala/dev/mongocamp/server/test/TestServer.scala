package dev.mongocamp.server.test

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.RestServer
import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.service.ReflectionService
import dev.mongocamp.server.test.client.api.InformationApi

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object TestServer extends LazyLogging {

  private var serverRunning      = false
  private var mongoServerStarted = false

  System.setProperty("CONNECTION_HOST", "localhost")
  System.setProperty("CONNECTION_DATABASE", "test")
  System.setProperty("PLUGINS_DIRECTORY", File.temporaryDirectory().get().toString())

  var retries = 0

  lazy val server : RestServer = {
    val servers = ReflectionService.instancesForType(classOf[RestServer])
    if (servers.size == 1) {
      servers.head
    } else {
      throw new Exception("more than one implementation for rest server found")
    }
  }

  while (!serverRunning) {
    try {
      if (!mongoServerStarted) {
        Future.successful {
          MongoTestServer.startMongoDatabase()
          setPort()
          server.registerMongoCampServerDefaultConfigs
          server.startServer()(ExecutionContext.global)
        }
        mongoServerStarted = true
      }
      val versionRequest  = InformationApi().version()
      val versionFuture   = TestAdditions.backend.send(versionRequest)
      versionFuture.body.getOrElse(throw new Exception("error"))
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

  def serverBaseUrl: String = {
    "http://%s:%s".format(server.interface, server.port)
//    "http://%s:%s".format(server.interface, 8080)
  }

  def setPort(): Unit = {
    val port = Random.nextInt(10000) + TestAdditions.minPort
    System.setProperty("SERVER_PORT", port.toString)
  }
}
