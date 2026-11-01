package dev.mongocamp.server.event

import better.files.Resource
import com.typesafe.scalalogging.LazyLogging
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

object EventSystem extends LazyLogging {

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

  // an event is only a notification: a failure to send it (for example a class that is not registered for serialization in the
  // native image) must never turn a request that already changed data into an error response
  def publish(event: Event): Unit = {
    Try(channel.send(new ObjectMessage(null, event))).failed.foreach(
      ex => logger.error("could not publish event <%s>".format(event.getClass.getName), ex)
    )
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
