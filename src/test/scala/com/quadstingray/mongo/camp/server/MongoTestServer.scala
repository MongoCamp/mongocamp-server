package com.quadstingray.mongo.camp.server

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import de.flapdoodle.embed.mongo.config._
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.packageresolver.Command
import de.flapdoodle.embed.mongo.{ MongodExecutable, MongodStarter }
import de.flapdoodle.embed.process.config.process.ProcessOutput
import de.flapdoodle.embed.process.io.{ Processors, Slf4jLevel }
import de.flapdoodle.embed.process.runtime.Network

import scala.util.Random

object MongoTestServer extends LazyLogging {
  private var running: Boolean = false

  lazy val mongoPort: Int = {
    val port = Random.nextInt(10000)
    System.setProperty("CONNECTION_PORT", port.toString)
    port
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

  def startMongoDatabase(): Unit = {
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
