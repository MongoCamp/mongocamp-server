package dev.mongocamp.server.event.config

import dev.mongocamp.server.event.Event

case class ConfigRegisterEvent(
    persistent: Boolean,
    configKey: String,
    configType: String,
    value: Option[Any] = None,
    comment: String = "",
    needsRestartForActivation: Boolean = false
) extends Event
