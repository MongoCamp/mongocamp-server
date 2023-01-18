package dev.mongocamp.server.monitoring.plugin

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.micrometer.mongodb.registry.MongoStepMeterRegistry
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.monitoring.MetricsConfiguration
import dev.mongocamp.server.plugin.ServerPlugin

object MongoDbMetricsPlugin extends ServerPlugin with LazyLogging {

  private lazy val databaseProvider = MongoDatabase.createNewDatabaseProvider(false)

  override def activate(): Unit = {
    MetricsConfiguration.addJvmRegistry(MongoStepMeterRegistry(databaseProvider.dao("monitoring_jvm")))
    MetricsConfiguration.addSystemRegistry(MongoStepMeterRegistry(databaseProvider.dao("monitoring_system")))
    MetricsConfiguration.addMongoRegistry(MongoStepMeterRegistry(databaseProvider.dao("monitoring_mongo_db")))
    MetricsConfiguration.addEventRegistry(MongoStepMeterRegistry(databaseProvider.dao("monitoring_event")))
  }

}
