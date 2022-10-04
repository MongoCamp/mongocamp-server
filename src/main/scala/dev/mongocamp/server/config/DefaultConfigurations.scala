package dev.mongocamp.server.config

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.config.ConfigManager.{registerConfig, registerNonPersistentConfig}
import dev.mongocamp.server.database.ConfigDao

import scala.util.Random

object DefaultConfigurations {
  lazy val ConfigKeyServerInterface      = "server.interface"
  lazy val ConfigKeyServerPort           = "server.port"
  lazy val ConfigKeyRequestLogging       = "requestlogging.enabled"
  lazy val ConfigKeyPluginsIgnored       = "plugins.ignored"
  lazy val ConfigKeyPluginsDirectory     = "plugins.directory"
  lazy val ConfigKeyAuthHandler          = "auth.handler"
  lazy val ConfigKeyAuthSecret           = "auth.secret"
  lazy val ConfigKeyAuthApiKeyLength     = "auth.apikeylength"
  lazy val ConfigKeyAuthCacheDb          = "auth.cache.db"
  lazy val ConfigKeyAuthExpiringDuration = "auth.expiring.duration"
  lazy val ConfigKeyAuthPrefix           = "auth.prefix"
  lazy val ConfigKeyAuthBearer           = "auth.bearer"
  lazy val ConfigKeyAuthBasic            = "auth.basic"
  lazy val ConfigKeyAuthToken            = "auth.token"
  lazy val ConfigKeyAuthUsers            = "auth.users"
  lazy val ConfigKeyAuthRoles            = "auth.roles"
  lazy val ConfigKeyConnectionHost       = "connection.host"
  lazy val ConfigKeyConnectionPort       = "connection.port"
  lazy val ConfigKeyConnectionDatabase   = "connection.database"
  lazy val ConfigKeyConnectionUsername   = "connection.username"
  lazy val ConfigKeyConnectionPassword   = "connection.password"
  lazy val ConfigKeyConnectionAuthDb     = "connection.authdb"
  lazy val ConfigKeyFileHandler          = "file.handler"
  lazy val ConfigKeyFileCache            = "file.cache.age"
  lazy val ConfigKeyCorsHeadersAllowed   = "cors.headers.allowed"
  lazy val ConfigKeyCorsHeadersExposed   = "cors.headers.exposed"
  lazy val ConfigKeyCorsOriginsAllowed   = "cors.origins.allowed"
  lazy val ConfigKeyDocsSwagger          = "docs.swagger"
  lazy val ConfigKeyOpenApi              = "docs.openapi"

  def registerMongoCampServerDefaultConfigs(): Unit = {
    registerNonPersistentConfig(ConfigKeyConnectionHost, MongoCampConfiguration.confTypeString)
    registerNonPersistentConfig(ConfigKeyConnectionPort, MongoCampConfiguration.confTypeLong)
    registerNonPersistentConfig(ConfigKeyConnectionDatabase, MongoCampConfiguration.confTypeString)
    registerNonPersistentConfig(ConfigKeyConnectionUsername, s"Option[${MongoCampConfiguration.confTypeString}]")
    registerNonPersistentConfig(ConfigKeyConnectionPassword, s"Option[${MongoCampConfiguration.confTypeString}]")
    registerNonPersistentConfig(ConfigKeyConnectionAuthDb, MongoCampConfiguration.confTypeString)
    registerNonPersistentConfig(ConfigKeyAuthPrefix, MongoCampConfiguration.confTypeString)

    ConfigDao().createIndex(Map("key" -> 1)).result()

    registerConfig(ConfigKeyServerInterface, MongoCampConfiguration.confTypeString, needsRestartForActivation = true)
    registerConfig(ConfigKeyServerPort, MongoCampConfiguration.confTypeLong, needsRestartForActivation = true)
    registerConfig(ConfigKeyRequestLogging, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)

    registerConfig(ConfigKeyPluginsIgnored, s"List[${MongoCampConfiguration.confTypeString}]", needsRestartForActivation = true)
    registerConfig(ConfigKeyPluginsDirectory, MongoCampConfiguration.confTypeString, needsRestartForActivation = true)

    registerConfig(ConfigKeyAuthHandler, MongoCampConfiguration.confTypeString, needsRestartForActivation = true)
    registerConfig(ConfigKeyAuthSecret, MongoCampConfiguration.confTypeString, Some(Random.alphanumeric.take(32).mkString))
    registerConfig(ConfigKeyAuthApiKeyLength, MongoCampConfiguration.confTypeLong)
    registerConfig(ConfigKeyAuthCacheDb, MongoCampConfiguration.confTypeBoolean)
    registerConfig(ConfigKeyAuthExpiringDuration, MongoCampConfiguration.confTypeDuration)

    registerConfig(ConfigKeyAuthBearer, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)
    registerConfig(ConfigKeyAuthBasic, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)
    registerConfig(ConfigKeyAuthToken, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)
    registerConfig(ConfigKeyAuthUsers, s"List[${MongoCampConfiguration.confTypeString}]", needsRestartForActivation = true)
    registerConfig(ConfigKeyAuthRoles, s"List[${MongoCampConfiguration.confTypeString}]", needsRestartForActivation = true)

    registerConfig(ConfigKeyFileHandler, MongoCampConfiguration.confTypeString)
    registerConfig(ConfigKeyFileCache, MongoCampConfiguration.confTypeString)

    registerConfig(ConfigKeyCorsHeadersAllowed, s"List[${MongoCampConfiguration.confTypeString}]")
    registerConfig(ConfigKeyCorsHeadersExposed, s"List[${MongoCampConfiguration.confTypeString}]")
    registerConfig(ConfigKeyCorsOriginsAllowed, s"List[${MongoCampConfiguration.confTypeString}]")
    registerConfig(ConfigKeyDocsSwagger, MongoCampConfiguration.confTypeBoolean)
    registerConfig(ConfigKeyOpenApi, MongoCampConfiguration.confTypeBoolean)
  }
}
