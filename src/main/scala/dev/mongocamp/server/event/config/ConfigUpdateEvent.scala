package dev.mongocamp.server.event.config

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.UserInformation

case class ConfigUpdateEvent(key: String, newValue: Any, oldValue: Any, callingMethod: String) extends Event
