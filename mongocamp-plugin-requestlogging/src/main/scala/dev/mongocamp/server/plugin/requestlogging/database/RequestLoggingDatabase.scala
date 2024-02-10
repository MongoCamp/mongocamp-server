package dev.mongocamp.server.plugin.requestlogging.database

import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.service.ConfigurationService

private[requestlogging] object RequestLoggingDatabase {
  private[requestlogging] lazy val collectionPrefix                     = ConfigurationService.getConfigValue[String](DefaultConfigurations.ConfigKeyAuthPrefix)
  private[requestlogging] lazy val CollectionNameRequestLog             = s"${collectionPrefix}request_logging"
  private[requestlogging] lazy val requestLoggingDao: RequestLoggingDao = RequestLoggingDao()
}
