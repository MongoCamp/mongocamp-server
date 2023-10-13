package dev.mongocamp.server.event.config

import dev.mongocamp.server.event.Event
case class ConfigUpdateEvent(key: String, newValue: Any, oldValue: Any, callingMethod: String) extends Event
