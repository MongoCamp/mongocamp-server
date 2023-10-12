package dev.mongocamp.server.interceptor

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.exception.MongoCampException
import sttp.tapir.server.interceptor.log.DefaultServerLog

import scala.concurrent.Future

object MongoCampHttpServerLog extends LazyLogging {

  def serverLog(): DefaultServerLog[Future] = {

    def debugLog(msg: String, exOpt: Option[Throwable]): Future[Unit] = Future.successful {
      exOpt match {
        case None     => logger.debug(msg)
        case Some(ex) => logger.debug(s"$msg; exception: {}", ex)
      }
    }

    def errorLog(msg: String, ex: Throwable): Future[Unit] = Future.successful {
      if (ex.isInstanceOf[MongoCampException]) {
        logger.debug(msg, ex)
      }
      else {
        logger.error(msg, ex)
      }
    }

    DefaultServerLog[Future](
      doLogWhenReceived = debugLog(_, None),
      doLogWhenHandled = debugLog,
      doLogAllDecodeFailures = debugLog,
      doLogExceptions = errorLog,
      noLog = Future.successful(())
    )
  }

}
