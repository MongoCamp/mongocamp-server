package dev.mongocamp.server.plugins.monitoring.mongodb

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.micrometer.mongodb.registry.MongoStepMeterRegistry
import dev.mongocamp.server.Server
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.model.MongoCampConfiguration
import dev.mongocamp.server.plugin.ServerPlugin
import dev.mongocamp.server.plugins.monitoring.MetricsConfiguration
import dev.mongocamp.server.service.ConfigurationService

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration

object MetricsMongoDBPersistenceLoggingPlugin extends ServerPlugin with LazyLogging {
  private val ConfKeyMicrometerStep         = "LOGGING_METRICS_MONGODB_STEP"
  private val ConfKeyLoggingJvmToMongoDb    = "LOGGING_METRICS_MONGODB_JVM"
  private val ConfKeyLoggingSystemToMongoDb = "LOGGING_METRICS_MONGODB_SYSTEM"
  private val ConfKeyLoggingMongoToMongoDb  = "LOGGING_METRICS_MONGODB_MONGO"
  private val ConfKeyLoggingEventToMongoDb  = "LOGGING_METRICS_MONGODB_EVENT"

  override def activate(): Unit = {

    ConfigurationService.registerConfig(ConfKeyMicrometerStep, MongoCampConfiguration.confTypeDuration)
    ConfigurationService.registerConfig(ConfKeyLoggingJvmToMongoDb, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyLoggingSystemToMongoDb, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyLoggingMongoToMongoDb, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyLoggingEventToMongoDb, MongoCampConfiguration.confTypeBoolean)

    val stepDuration = ConfigurationService.getConfigValue[Duration](ConfKeyMicrometerStep)
    val configMap    = Map("step" -> s"${stepDuration.toMillis}ms")
    val registriesList : ArrayBuffer[MongoStepMeterRegistry]= ArrayBuffer()
    if (ConfigurationService.getConfigValue[Boolean](ConfKeyLoggingJvmToMongoDb)) {
      val registry = MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_jvm"), configMap)
      registriesList.addOne(registry)
      MetricsConfiguration.addJvmRegistry(registry)
    }
    if (ConfigurationService.getConfigValue[Boolean](ConfKeyLoggingSystemToMongoDb)) {
      val registry = MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_system"), configMap)
      registriesList.addOne(registry)
      MetricsConfiguration.addSystemRegistry(registry)
    }
    if (ConfigurationService.getConfigValue[Boolean](ConfKeyLoggingMongoToMongoDb)) {
      val registry = MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_mongo_db"), configMap)
      registriesList.addOne(registry)
      MetricsConfiguration.addMongoRegistry(registry)
    }
    if (ConfigurationService.getConfigValue[Boolean](ConfKeyLoggingEventToMongoDb)) {
      val registry = MongoStepMeterRegistry(MongoDatabase.databaseProvider.dao("monitoring_event"), configMap)
      registriesList.addOne(registry)
      MetricsConfiguration.addEventRegistry(registry)
    }

    Server.registerServerShutdownCallBacks(
      () => {
        registriesList.foreach(r => {
          println(s"Publishing metrics for to close Registry")
          r.publish()
          r.stop()
          r.close()
        })
      }
    )
  }

}
