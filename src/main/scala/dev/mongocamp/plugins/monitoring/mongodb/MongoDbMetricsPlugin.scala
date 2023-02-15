package dev.mongocamp.plugins.monitoring.mongodb

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.micrometer.mongodb.registry.MongoStepMeterRegistry
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.model.MongoCampConfiguration
import dev.mongocamp.server.monitoring.MetricsConfiguration
import dev.mongocamp.server.plugin.ServerPlugin
import dev.mongocamp.server.service.ConfigurationService

import scala.concurrent.duration.Duration

object MongoDbMetricsPlugin extends ServerPlugin with LazyLogging {
  private val ConfKeyMicrometerStep = "micrometer.mongodb.step"
  private val ConfKeyMonitorJVM     = "micrometer.mongodb.jvm"
  private val ConfKeyMonitorSystem  = "micrometer.mongodb.system"
  private val ConfKeyMonitorMongo   = "micrometer.mongodb.mongo"
  private val ConfKeyMonitorEvent   = "micrometer.mongodb.event"

  override def activate(): Unit = {

    ConfigurationService.registerConfig(ConfKeyMonitorJVM, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyMonitorSystem, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyMonitorMongo, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyMonitorEvent, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyMicrometerStep, MongoCampConfiguration.confTypeDuration)

    val step      = java.time.Duration.ofSeconds(ConfigurationService.getConfigValue[Duration](ConfKeyMicrometerStep).toSeconds)
    val configMap = Map("step" -> step.toString)
    if (ConfigurationService.getConfigValue[Boolean](ConfKeyMonitorJVM)) {
      MetricsConfiguration.addJvmRegistry(MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_jvm"), configMap))
    }
    if (ConfigurationService.getConfigValue[Boolean](ConfKeyMonitorSystem)) {
      MetricsConfiguration.addSystemRegistry(MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_system"), configMap))
    }
    if (ConfigurationService.getConfigValue[Boolean](ConfKeyMonitorMongo)) {
      MetricsConfiguration.addMongoRegistry(MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_mongo_db"), configMap))
    }
    if (ConfigurationService.getConfigValue[Boolean](ConfKeyMonitorEvent)) {
      MetricsConfiguration.addEventRegistry(MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_event"), configMap))
    }
  }

}
