package dev.mongocamp.server.test

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.{ Mongod, RunningMongodProcess }
import de.flapdoodle.embed.mongo.types.DatabaseDir
import de.flapdoodle.embed.process.io.ProcessOutput
import de.flapdoodle.reverse.TransitionWalker
import de.flapdoodle.reverse.transitions.Start
import dev.mongocamp.server.database.TestAdditions
import sttp.client3._
import sttp.model.Method

import scala.util.Random

object MongoTestServer extends LazyLogging {
  private var running: Boolean = false

  var mongoPort: Int = setPort()

  var process: TransitionWalker.ReachedState[RunningMongodProcess] = null

  def setPort(): Int = {
    val port = Random.nextInt(10000) + TestAdditions.minPort
    System.setProperty("CONNECTION_PORT", port.toString)
    mongoPort = port
    mongoPort
  }

  private var mongodExecutable: Mongod = initMonoExecutable

  private var tempDir = File.newTemporaryDirectory()

  private def initMonoExecutable: Mongod = {
    val mongod = Mongod
      .builder()
      .net(Start.to(classOf[Net]).providedBy(() => Net.builder().port(mongoPort).bindIp(Net.defaults().getBindIp).isIpv6(false).build()))
      .databaseDir(Start.to(classOf[DatabaseDir]).providedBy(() => DatabaseDir.of(tempDir.path)))
      .processOutput(Start.to(classOf[ProcessOutput]).providedBy(() => ProcessOutput.silent()))
      .build()
    mongod
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
      process = mongodExecutable.start(Version.V7_0_4)
      running = true
      sys.addShutdownHook({
        println("Shutdown for MongoDB Server triggered.")
        stopMongoDatabase()
      })
    }
  }

  def stopMongoDatabase(): Unit = {
    if (running) {
      process.current().stop()
      process = null
      running = false
      tempDir.delete()
      tempDir = File.newTemporaryDirectory()
      mongodExecutable = initMonoExecutable
    }
  }

}
