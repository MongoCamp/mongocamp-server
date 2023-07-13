package dev.mongocamp.server.event.server

import dev.mongocamp.server.event.Event

case class PluginLoadedEvent(pluginName: String, pluginType: String) extends Event
