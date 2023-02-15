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

  override def activate(): Unit = {
    ConfigurationService.registerConfig("micrometer.mongodb.jvm", MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig("micrometer.mongodb.system", MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig("micrometer.mongodb.mongo", MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig("micrometer.mongodb.event", MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig("micrometer.mongodb.step", MongoCampConfiguration.confTypeDuration)

    val step      = java.time.Duration.ofSeconds(ConfigurationService.getConfigValue[Duration]("micrometer.mongodb.step").toSeconds)
    val configMap = Map("step" -> step.toString)
    if (ConfigurationService.getConfigValue[Boolean]("micrometer.mongodb.jvm")) {
      MetricsConfiguration.addJvmRegistry(MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_jvm"), configMap))
    }
    if (ConfigurationService.getConfigValue[Boolean]("micrometer.mongodb.system")) {
      MetricsConfiguration.addSystemRegistry(MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_system"), configMap))
    }
    if (ConfigurationService.getConfigValue[Boolean]("micrometer.mongodb.mongo")) {
      MetricsConfiguration.addMongoRegistry(MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_mongo_db"), configMap))
    }
    if (ConfigurationService.getConfigValue[Boolean]("micrometer.mongodb.event")) {
      MetricsConfiguration.addEventRegistry(MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_event"), configMap))
    }
  }

}
