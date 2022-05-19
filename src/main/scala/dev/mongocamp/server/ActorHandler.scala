package dev.mongocamp.server
import akka.actor.ActorSystem

import scala.concurrent.ExecutionContextExecutor

object ActorHandler {

  lazy val requestActorSystem: ActorSystem = ActorSystem("mongocamp-server-requests")

  def requestExecutionContext: ExecutionContextExecutor = requestActorSystem.dispatcher

}
