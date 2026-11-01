package dev.mongocamp.server.tests

import better.files.Resource
import dev.mongocamp.server.event.Event
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.service.reflection.ClassGraphService
import java.lang.reflect.Modifier
import munit.FunSuite

// the native image only serializes classes that are registered in serialization-config.json, events are serialized when they are published
class EventSystemSuite extends FunSuite {

  private lazy val registeredTypes: Set[String] =
    "\"name\"\\s*:\\s*\"([^\"]+)\"".r
      .findAllMatchIn(Resource.getAsString("META-INF/native-image/dev.mongocamp/mongocamp-server/serialization-config.json"))
      .map(_.group(1))
      .toSet

  test("every event class is registered for serialization in the native image") {
    val eventClasses = new ClassGraphService()
      .getClassesForPackage[Any]("dev.mongocamp.server.event")
      .filter(
        c => classOf[Event].isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers)
      )
    assert(eventClasses.nonEmpty, "no event classes found")
    val missing = eventClasses.map(_.getName).filterNot(registeredTypes.contains)
    assertEquals(missing, List.empty[String], "events missing in serialization-config.json")
  }

  test("collections that are part of events are registered for serialization in the native image") {
    // a document with two fields is a Map2, the old values of a changed document are part of the events
    val missing = List("scala.collection.immutable.Map$Map2", "scala.collection.immutable.Map$EmptyMap$").filterNot(registeredTypes.contains)
    assertEquals(missing, List.empty[String])
  }

  test("a failing publish does not throw") {
    class NotSerializableEvent extends Event { val value = new Object() }
    EventSystem.publish(new NotSerializableEvent())
  }
}
