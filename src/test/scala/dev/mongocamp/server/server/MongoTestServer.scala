package dev.mongocamp.server.server

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import de.flapdoodle.embed.mongo.config._
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.packageresolver.Command
import de.flapdoodle.embed.mongo.{MongodExecutable, MongodStarter}
import de.flapdoodle.embed.process.config.process.ProcessOutput
import de.flapdoodle.embed.process.io.{Processors, Slf4jLevel}
import de.flapdoodle.embed.process.runtime.Network
import sttp.client3.basicRequest
import sttp.model.Method

import scala.util.Random
import dev.mongocamp.server.client.core.JsonSupport._
import dev.mongocamp.server.client.model._
import dev.mongocamp.server.converter.CirceSchema
import dev.mongocamp.server.server.TestServer
import sttp.client3._
import sttp.client3.circe.asJson
import sttp.model.Method

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object MongoTestServer extends LazyLogging {
  private var running: Boolean = false

  var mongoPort: Int = setPort()

  def setPort(): Int = {
    val port = Random.nextInt(10000) + TestAdditions.minPort
    System.setProperty("CONNECTION_PORT", port.toString)
    mongoPort = port
    mongoPort
  }

  private var mongodExecutable: MongodExecutable = initMonoExecutable

  private var tempDir = File.newTemporaryDirectory()

  private def initMonoExecutable: MongodExecutable = {
    val processOutput = ProcessOutput
      .builder()
      .output(Processors.logTo(logger.underlying, Slf4jLevel.INFO))
      .error(Processors.logTo(logger.underlying, Slf4jLevel.ERROR))
      .commands(Processors.named("[console>]", Processors.logTo(logger.underlying, Slf4jLevel.DEBUG)))
      .build();

    tempDir = File.newTemporaryDirectory()

    MongodStarter
      .getInstance(Defaults.runtimeConfigFor(Command.MongoD, logger.underlying).processOutput(processOutput).build())
      .prepare(
        ImmutableMongodConfig
          .builder()
          .version(Version.Main.PRODUCTION)
          .net(new Net("localhost", mongoPort, Network.localhostIsIPv6()))
          .replication(new Storage(tempDir.pathAsString, null, 0))
          .build()
      )
  }

  def isRunning: Boolean = running

  def checkForLocalRunningMongoDb(): Unit = {
    if (!running) {
      try {
        val checkRequest = basicRequest.method(Method.GET, uri"http://localhost:4711").response(asString)
        val resultFuture = TestAdditions.backend.send(checkRequest)
        val responseResult = Await.result(resultFuture, 1.seconds).body.getOrElse("not found")
        if (responseResult.contains("HTTP on the native driver port.")) {
          println("Use local running MongoDb")
          System.setProperty("CONNECTION_PORT", "4711")
          running = true
        }
      } catch {
        case _: Exception =>
          ""
      }
    }

  }

  def startMongoDatabase(): Unit = {
    checkForLocalRunningMongoDb()
    if (!running) {
      mongodExecutable.start()
      running = true
      sys.addShutdownHook({
        println("Shutdown for MongoDB Server triggered.")
        stopMongoDatabase()
      })
    }
  }

  def stopMongoDatabase(): Unit = {
    if (running) {
      mongodExecutable.stop()
      running = false
      tempDir.delete()
      mongodExecutable = initMonoExecutable
    }
  }

}
