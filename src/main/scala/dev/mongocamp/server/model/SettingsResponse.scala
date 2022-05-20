package dev.mongocamp.server.model

case class SettingsResponse(routesPlugins: List[String], filePlugins: List[String], ignoredPlugins: List[String], configurations: Map[String, Any])
