package dev.mongocamp.plugins.monitoring.mongodb

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.micrometer.mongodb.registry.MongoStepMeterRegistry
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.model.MongoCampConfiguration
import dev.mongocamp.server.monitoring.MetricsConfiguration
import dev.mongocamp.server.plugin.ServerPlugin
import dev.mongocamp.server.service.ConfigurationService

import scala.concurrent.duration.Duration

object MongoDbMetricsLoggingPlugin extends ServerPlugin with LazyLogging {
  private val ConfKeyMicrometerStep         = "micrometer.mongodb.step"
  private val ConfKeyLoggingJvmToMongoDb    = "micrometer.mongodb.jvm"
  private val ConfKeyLoggingSystemToMongoDb = "micrometer.mongodb.system"
  private val ConfKeyLoggingMongoToMongoDb  = "micrometer.mongodb.mongo"
  private val ConfKeyLoggingEventToMongoDb  = "micrometer.mongodb.event"

  override def activate(): Unit = {

    ConfigurationService.registerConfig(ConfKeyMicrometerStep, MongoCampConfiguration.confTypeDuration)
    ConfigurationService.registerConfig(ConfKeyLoggingJvmToMongoDb, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyLoggingSystemToMongoDb, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyLoggingMongoToMongoDb, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyLoggingEventToMongoDb, MongoCampConfiguration.confTypeBoolean)

    val step      = java.time.Duration.ofSeconds(ConfigurationService.getConfigValue[Duration](ConfKeyMicrometerStep).toSeconds)
    val configMap = Map("step" -> step.toString)
    if (ConfigurationService.getConfigValue[Boolean](ConfKeyLoggingJvmToMongoDb)) {
      MetricsConfiguration.addJvmRegistry(MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_jvm"), configMap))
    }
    if (ConfigurationService.getConfigValue[Boolean](ConfKeyLoggingSystemToMongoDb)) {
      MetricsConfiguration.addSystemRegistry(MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_system"), configMap))
    }
    if (ConfigurationService.getConfigValue[Boolean](ConfKeyLoggingMongoToMongoDb)) {
      MetricsConfiguration.addMongoRegistry(MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_mongo_db"), configMap))
    }
    if (ConfigurationService.getConfigValue[Boolean](ConfKeyLoggingEventToMongoDb)) {
      MetricsConfiguration.addEventRegistry(MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_event"), configMap))
    }
  }

}
