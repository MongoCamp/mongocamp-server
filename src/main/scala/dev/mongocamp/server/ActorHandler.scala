package dev.mongocamp.server
import akka.actor.ActorSystem
import akka.event.EventStream

import scala.concurrent.ExecutionContextExecutor

object ActorHandler {

  lazy val requestActorSystem: ActorSystem = ActorSystem("mongocamp-server-requests")

  def requestExecutionContext: ExecutionContextExecutor = requestActorSystem.dispatcher

  private lazy val eventBusActorSystem: ActorSystem = ActorSystem("mongocamp-server-event")

  def eventStream: EventStream = eventBusActorSystem.eventStream

}
