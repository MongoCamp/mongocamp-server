package dev.mongocamp.server.plugins.monitoring.mongodb

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.micrometer.mongodb.binder._
import dev.mongocamp.server.Server
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.model.MongoCampConfiguration
import dev.mongocamp.server.plugin.ServerPlugin
import dev.mongocamp.server.plugins.monitoring.MetricsConfiguration
import dev.mongocamp.server.service.ConfigurationService
import io.micrometer.core.instrument.binder.mongodb.{ MongoMetricsCommandListener, MongoMetricsConnectionPoolListener }

import scala.collection.mutable.ArrayBuffer

object MongoDbMetricsPlugin extends ServerPlugin with LazyLogging {

  private val ConfKeyMetricsDatabase       = "METRICS_MONGODB_DATABASE"
  private val ConfKeyMetricsCollectionList = "METRICS_MONGODB_COLLECTIONS"
  private val ConfKeyMetricsConnections    = "METRICS_MONGODB_CONNECTIONS"
  private val ConfKeyMetricsNetwork        = "METRICS_MONGODB_NETWORK"
  private val ConfKeyMetricsOperation      = "METRICS_MONGODB_OPERATION"
  private val ConfKeyMetricsServer         = "METRICS_MONGODB_SERVER"
  private val ConfKeyMetricsCommand        = "METRICS_MONGODB_COMMAND"
  private val ConfKeyMetricsConnectionpool = "METRICS_MONGODB_CONNECTIONPOOL"

  override def activate(): Unit = {
    ConfigurationService.registerConfig(ConfKeyMetricsDatabase, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyMetricsConnections, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyMetricsNetwork, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyMetricsOperation, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyMetricsServer, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyMetricsCollectionList, MongoCampConfiguration.confTypeStringList)
    ConfigurationService.registerConfig(ConfKeyMetricsCommand, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyMetricsConnectionpool, MongoCampConfiguration.confTypeBoolean)

    if (ConfigurationService.getConfigValue[Boolean](ConfKeyMetricsDatabase)) {
      MetricsConfiguration.addMongoDbBinder(DatabaseMetrics(MongoDatabase.databaseProvider.database()))
    }

    if (ConfigurationService.getConfigValue[Boolean](ConfKeyMetricsConnections)) {
      MetricsConfiguration.addMongoDbBinder(ConnectionsMetrics(MongoDatabase.databaseProvider.database()))
    }

    if (ConfigurationService.getConfigValue[Boolean](ConfKeyMetricsNetwork)) {
      MetricsConfiguration.addMongoDbBinder(NetworkMetrics(MongoDatabase.databaseProvider.database()))
    }

    if (ConfigurationService.getConfigValue[Boolean](ConfKeyMetricsOperation)) {
      MetricsConfiguration.addMongoDbBinder(OperationMetrics(MongoDatabase.databaseProvider.database()))
    }

    if (ConfigurationService.getConfigValue[Boolean](ConfKeyMetricsServer)) {
      MetricsConfiguration.addMongoDbBinder(ServerMetrics(MongoDatabase.databaseProvider.database()))
    }

    val registerFunctionsList: ArrayBuffer[() => Unit] = ArrayBuffer()

    if (ConfigurationService.getConfigValue[Boolean](ConfKeyMetricsCommand)) {
      registerFunctionsList += { () =>
        {
          MetricsConfiguration.getMongoDbMetricsRegistries.foreach(r => {
            val commandListener = new MongoMetricsCommandListener(r)
            MongoDatabase.registerCommandListener(commandListener)
          })
        }
      }
    }

    if (ConfigurationService.getConfigValue[Boolean](ConfKeyMetricsConnectionpool)) {
      registerFunctionsList += { () =>
        {
          MetricsConfiguration.getMongoDbMetricsRegistries.foreach(r => {
            val commandListener = new MongoMetricsConnectionPoolListener(r)
            MongoDatabase.registerConnectionPoolListener(commandListener)
          })
        }
      }
    }

    ConfigurationService
      .getConfigValue[List[String]](ConfKeyMetricsCollectionList)
      .foreach(collection => MetricsConfiguration.addMongoDbBinder(CollectionMetrics(MongoDatabase.databaseProvider.database(), collection)))

    Server.registerAfterStartCallBack(() => {
      MetricsConfiguration.bindAll()
      registerFunctionsList.foreach(f => f())
      MongoDatabase.createNewDatabaseProvider()
    })
  }

}
