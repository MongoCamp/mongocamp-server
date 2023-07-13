package dev.mongocamp.server.event

import java.util.{Date, UUID}

abstract class Event {
  val eventDate  = new Date()
  val id: String = UUID.randomUUID().toString
}
