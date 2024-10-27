package dev.mongocamp.server.test

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.database.TestAdditions
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import sttp.client3._
import sttp.model.Method

import scala.concurrent.duration.DurationInt
import scala.jdk.DurationConverters.ScalaDurationOps
import scala.util.Random

object MongoTestServer extends LazyLogging {
  private var running: Boolean = false

  private lazy val containerConfiguration: GenericContainer[_] = {
    val mongoDbContainer = new GenericContainer(s"mongocamp/mongodb:latest")
    mongoDbContainer.withExposedPorts(27017)
    mongoDbContainer.waitingFor(Wait.forLogMessage("(.*?)child process started successfully, parent exiting(.*?)", 2).withStartupTimeout(60.seconds.toJava))
    mongoDbContainer
  }

  def isRunning: Boolean = running

  def checkForLocalRunningMongoDb(): Unit = {
    if (!running) {
      try {
        val checkRequest   = basicRequest.method(Method.GET, uri"http://localhost:4711").response(asString)
        val responseResult = TestAdditions.backend.send(checkRequest).body.getOrElse("not found")
        if (responseResult.contains("HTTP on the native driver port.")) {
          println("Use local running MongoDb")
          System.setProperty("CONNECTION_PORT", "4711")
          running = true
        }
      }
      catch {
        case e: Exception =>
          e.getMessage
      }
    }

  }

  def startMongoDatabase(): Unit = {
    checkForLocalRunningMongoDb()
    if (!running) {
      try
        containerConfiguration.start()
      catch {
        case _: Throwable =>
      }
      System.setProperty("CONNECTION_PORT", containerConfiguration.getMappedPort(27017).toString)
      System.setProperty("CONNECTION_HOST", containerConfiguration.getHost)
      running = true
      sys.addShutdownHook({
        println("Shutdown for MongoDB Server triggered.")
        stopMongoDatabase()
      })

    }
  }

  def stopMongoDatabase(): Unit = {
    if (running) {
      containerConfiguration.stop()
      running = false
    }
  }

}
