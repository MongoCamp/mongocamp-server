package dev.mongocamp.server

import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait RestServer extends LazyLogging {

  // def instead of val: implementations may be lazy (Scala 3 does not allow a lazy val to override a strict val)
  def interface: String

  def port: Int

  def startServer()(implicit ex: ExecutionContext): Future[Unit]

  def registerMongoCampServerDefaultConfigs(): Unit

  def registerServerShutdownCallBacks(f: () => Unit): Unit

  def shutdown(): Unit

  sys.addShutdownHook(
    () => shutdown()
  )
}
