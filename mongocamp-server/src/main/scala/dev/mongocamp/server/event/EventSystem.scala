package dev.mongocamp.server.event

import better.files.Resource
import dev.mongocamp.server.Server
import org.apache.pekko.actor.ActorRef
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.actor.Props
import org.jgroups.Address
import org.jgroups.JChannel
import org.jgroups.Message
import org.jgroups.ObjectMessage
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._
import scala.util.Try

object EventSystem {

  private lazy val listeners: ArrayBuffer[(ActorRef, Class[_ <: Event])] = ArrayBuffer()

  private val channel: JChannel = {
    val channel = new JChannel(Resource.getAsStream("jgroups.xml"))
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

    Server.registerServerShutdownCallBacks(
      () => shutdown()
    )

    channel.connect("mongocamp-all-events")
  }

  private[server] def shutdown(): Unit = {
    if (isCoordinator && listOfMembers.size == 1) {
      channel.close()
    }
    channel.disconnect()
    Try { eventBusActorSystem.terminate() }
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

  def listOfMembers: List[Address] = {
    channel.view.getMembers.asScala.toList
  }

  def coordinator: Address = {
    channel.view.getCoord
  }

  def isCoordinator: Boolean = {
    channel.view.getCoord.equals(channel.address)
  }

}
