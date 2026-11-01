package dev.mongocamp.server.event

import java.util.Date
import java.util.UUID

abstract class Event {
  val eventDate  = new Date()
  val id: String = UUID.randomUUID().toString
}
