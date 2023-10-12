package dev.mongocamp.server.event

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.event.EventStream

object EventSystem {

  lazy val eventBusActorSystem: ActorSystem = ActorSystem("mongocamp-server-event")

  def eventStream: EventStream = eventBusActorSystem.eventStream

}
