package dev.mongocamp.server.event

import better.files.{File, Resource}
import org.apache.pekko.actor.{ActorRef, ActorSystem, Props}
import org.jgroups.{JChannel, Message, ObjectMessage}

import scala.collection.mutable.ArrayBuffer

object EventSystem {

  private lazy val listeners: ArrayBuffer[(ActorRef, Class[_ <: Event])] = ArrayBuffer()

  private val channel: JChannel = {
    val channel = new JChannel(Resource.getAsStream("jgroups-udp.xml"))
    channel.setReceiver(new org.jgroups.Receiver {

      override def receive(msg: Message): Unit = {
        listeners.foreach(
          listeners => {
            val actor = listeners._1
            val clazz = listeners._2
            if (clazz.isAssignableFrom(msg.getObject.getClass)) {
              actor ! msg.getObject.asInstanceOf[Event]
            }
          }
        )
      }

    })
    channel.connect("mongocamp-all-events")
  }

  private lazy val eventBusActorSystem: ActorSystem = {
    val system = ActorSystem("mongocamp-server-event")
    system
  }

  def publish(event: Event): Unit = {
    channel.send(new ObjectMessage(null, event))
  }

  def subscribe(actorRef: ActorRef, clazz: Class[_ <: Event]): Unit = {
    listeners += ((actorRef, clazz))
  }

  def startActor(props: Props, name: String): ActorRef = {
    eventBusActorSystem.actorOf(props, name)
  }

}
