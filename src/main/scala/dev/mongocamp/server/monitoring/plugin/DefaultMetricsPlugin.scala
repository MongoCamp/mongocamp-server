package dev.mongocamp.server.monitoring.plugin

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.monitoring.MetricsConfiguration
import dev.mongocamp.server.plugin.ServerPlugin
import io.micrometer.core.instrument.simple.SimpleMeterRegistry

object DefaultMetricsPlugin extends ServerPlugin with LazyLogging {
  override def activate(): Unit = {
    MetricsConfiguration.addJvmRegistry(new SimpleMeterRegistry())
    MetricsConfiguration.addSystemRegistry(new SimpleMeterRegistry())
    MetricsConfiguration.addMongoRegistry(new SimpleMeterRegistry())
    MetricsConfiguration.addEventRegistry(new SimpleMeterRegistry())
  }

}
