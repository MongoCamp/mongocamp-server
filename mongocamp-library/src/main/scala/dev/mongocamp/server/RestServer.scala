package dev.mongocamp.server

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ ExecutionContext, Future }

trait RestServer extends LazyLogging {

  val interface: String

  val port: Int

  def startServer()(implicit ex: ExecutionContext): Future[Unit]

  def registerMongoCampServerDefaultConfigs: Unit
}
