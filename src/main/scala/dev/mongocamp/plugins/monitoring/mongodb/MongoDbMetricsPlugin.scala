package dev.mongocamp.plugins.monitoring.mongodb

import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.micrometer.mongodb.binder.{ CollectionMetrics, ConnectionsMetrics, DatabaseMetrics, NetworkMetrics, OperationMetrics, ServerMetrics }
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.model.MongoCampConfiguration
import dev.mongocamp.server.monitoring.MetricsConfiguration
import dev.mongocamp.server.plugin.ServerPlugin
import dev.mongocamp.server.service.ConfigurationService

object MongoDbMetricsPlugin extends ServerPlugin with LazyLogging {

  private val ConfKeyMetricsDatabase       = "METRICS_MONGODB_DATABASE"
  private val ConfKeyMetricsCollectionList = "METRICS_MONGODB_COLLECTIONS"
  private val ConfKeyMetricsConnections    = "METRICS_MONGODB_CONNECTIONS"
  private val ConfKeyMetricsNetwork        = "METRICS_MONGODB_NETWORK"
  private val ConfKeyMetricsOperation      = "METRICS_MONGODB_OPERATION"
  private val ConfKeyMetricsServer         = "METRICS_MONGODB_SERVER"
  override def activate(): Unit = {
    ConfigurationService.registerConfig(ConfKeyMetricsDatabase, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyMetricsConnections, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyMetricsNetwork, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyMetricsOperation, MongoCampConfiguration.confTypeBoolean)
    ConfigurationService.registerConfig(ConfKeyMetricsServer, MongoCampConfiguration.confTypeBoolean)

    ConfigurationService.registerConfig(ConfKeyMetricsCollectionList, MongoCampConfiguration.confTypeStringList)

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

    ConfigurationService
      .getConfigValue[List[String]](ConfKeyMetricsCollectionList)
      .foreach(collection => MetricsConfiguration.addMongoDbBinder(CollectionMetrics(MongoDatabase.databaseProvider.database(), collection)))

  }

}
