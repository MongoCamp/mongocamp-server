package dev.mongocamp.server.plugin

trait ServerPlugin {
  def activate(): Unit
}
