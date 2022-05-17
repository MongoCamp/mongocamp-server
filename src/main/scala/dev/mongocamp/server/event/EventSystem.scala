package dev.mongocamp.server.event
import akka.actor.ActorSystem
import akka.event.EventStream

object EventSystem {

  lazy val eventBusActorSystem: ActorSystem = ActorSystem("mongocamp-server-event")

  def eventStream: EventStream = eventBusActorSystem.eventStream

}
