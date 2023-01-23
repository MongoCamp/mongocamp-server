package dev.mongocamp.server.config

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.ConfigDao
import dev.mongocamp.server.model.MongoCampConfiguration
import dev.mongocamp.server.service.ConfigurationService._

import scala.util.Random

object DefaultConfigurations {
  lazy val ConfigKeyServerInterface          = "SERVER_INTERFACE"
  lazy val ConfigKeyServerPort               = "SERVER_PORT"
  lazy val ConfigKeyRequestLogging           = "REQUESTLOGGING_ENABLED"
  lazy val ConfigKeyPluginsIgnored           = "PLUGINS_IGNORED"
  lazy val ConfigKeyPluginsDirectory         = "PLUGINS_DIRECTORY"
  lazy val ConfigKeyPluginsUrls              = "PLUGINS_URLS"
  lazy val ConfigKeyPluginsModules           = "PLUGINS_MODULES"
  lazy val ConfigKeyPluginsMavenRepositories = "PLUGINS_MAVEN_REPOSITORIES"
  lazy val ConfigKeyHttpClientHeaders        = "HTTP_CLIENT_HEADERS"
  lazy val ConfigKeyAuthHandler              = "AUTH_HANDLER"
  lazy val ConfigKeyAuthSecret               = "AUTH_SECRET"
  lazy val ConfigKeyAuthApiKeyLength         = "AUTH_APIKEYLENGTH"
  lazy val ConfigKeyAuthCacheDb              = "AUTH_CACHE_DB"
  lazy val ConfigKeyAuthExpiringDuration     = "AUTH_EXPIRING_DURATION"
  lazy val ConfigKeyAuthPrefix               = "AUTH_PREFIX"
  lazy val ConfigKeyAuthBearer               = "AUTH_BEARER"
  lazy val ConfigKeyAuthBasic                = "AUTH_BASIC"
  lazy val ConfigKeyAuthToken                = "AUTH_TOKEN"
  lazy val ConfigKeyAuthUsers                = "AUTH_USERS"
  lazy val ConfigKeyAuthRoles                = "AUTH_ROLES"
  lazy val ConfigKeyConnectionHost           = "CONNECTION_HOST"
  lazy val ConfigKeyConnectionPort           = "CONNECTION_PORT"
  lazy val ConfigKeyConnectionDatabase       = "CONNECTION_DATABASE"
  lazy val ConfigKeyConnectionUsername       = "CONNECTION_USERNAME"
  lazy val ConfigKeyConnectionPassword       = "CONNECTION_PASSWORD"
  lazy val ConfigKeyConnectionAuthDb         = "CONNECTION_AUTHDB"
  lazy val ConfigKeyFileHandler              = "FILE_HANDLER"
  lazy val ConfigKeyFileCache                = "FILE_CACHE_AGE"
  lazy val ConfigKeyCorsHeadersAllowed       = "CORS_HEADERS_ALLOWED"
  lazy val ConfigKeyCorsHeadersExposed       = "CORS_HEADERS_EXPOSED"
  lazy val ConfigKeyCorsOriginsAllowed       = "CORS_ORIGINS_ALLOWED"
  lazy val ConfigKeyDocsSwagger              = "DOCS_SWAGGER"
  lazy val ConfigKeyOpenApi                  = "DOCS_OPENAPI"

  def registerMongoCampServerDefaultConfigs(): Unit = {
    registerNonPersistentConfig(ConfigKeyConnectionHost, MongoCampConfiguration.confTypeString)
    registerNonPersistentConfig(ConfigKeyConnectionPort, MongoCampConfiguration.confTypeLong)
    registerNonPersistentConfig(ConfigKeyConnectionDatabase, MongoCampConfiguration.confTypeString)
    registerNonPersistentConfig(ConfigKeyConnectionUsername, s"Option[${MongoCampConfiguration.confTypeString}]")
    registerNonPersistentConfig(ConfigKeyConnectionPassword, s"Option[${MongoCampConfiguration.confTypeString}]")
    registerNonPersistentConfig(ConfigKeyConnectionAuthDb, MongoCampConfiguration.confTypeString)
    registerNonPersistentConfig(ConfigKeyAuthPrefix, MongoCampConfiguration.confTypeString)
    registerNonPersistentConfig(ConfigKeyAuthUsers, s"List[${MongoCampConfiguration.confTypeString}]")
    registerNonPersistentConfig(ConfigKeyAuthRoles, s"List[${MongoCampConfiguration.confTypeString}]")

    ConfigDao().createUniqueIndexForField("key").result()

    registerConfig(ConfigKeyServerInterface, MongoCampConfiguration.confTypeString, needsRestartForActivation = true)
    registerConfig(ConfigKeyServerPort, MongoCampConfiguration.confTypeLong, needsRestartForActivation = true)
    registerConfig(ConfigKeyRequestLogging, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)

    registerConfig(ConfigKeyPluginsIgnored, s"List[${MongoCampConfiguration.confTypeString}]", needsRestartForActivation = true)
    registerConfig(ConfigKeyPluginsUrls, s"List[${MongoCampConfiguration.confTypeString}]", needsRestartForActivation = true)
    registerConfig(ConfigKeyPluginsDirectory, MongoCampConfiguration.confTypeString, needsRestartForActivation = true)
    registerConfig(ConfigKeyPluginsModules, s"List[${MongoCampConfiguration.confTypeString}]", needsRestartForActivation = true)
    registerConfig(ConfigKeyPluginsMavenRepositories, s"List[${MongoCampConfiguration.confTypeString}]", needsRestartForActivation = true)
    registerConfig(ConfigKeyPluginIvyRepositories, s"List[${MongoCampConfiguration.confTypeString}]", needsRestartForActivation = true)

    registerConfig(ConfigKeyHttpClientHeaders, MongoCampConfiguration.confTypeString)

    registerConfig(ConfigKeyAuthHandler, MongoCampConfiguration.confTypeString, needsRestartForActivation = true)
    registerConfig(ConfigKeyAuthSecret, MongoCampConfiguration.confTypeString, Some(Random.alphanumeric.take(32).mkString))
    registerConfig(ConfigKeyAuthApiKeyLength, MongoCampConfiguration.confTypeLong)
    registerConfig(ConfigKeyAuthCacheDb, MongoCampConfiguration.confTypeBoolean)
    registerConfig(ConfigKeyAuthExpiringDuration, MongoCampConfiguration.confTypeDuration)

    registerConfig(ConfigKeyAuthBearer, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)
    registerConfig(ConfigKeyAuthBasic, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)
    registerConfig(ConfigKeyAuthToken, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)

    registerConfig(ConfigKeyFileHandler, MongoCampConfiguration.confTypeString, needsRestartForActivation = true)
    registerConfig(ConfigKeyFileCache, MongoCampConfiguration.confTypeString)

    registerConfig(ConfigKeyCorsHeadersAllowed, s"List[${MongoCampConfiguration.confTypeString}]")
    registerConfig(ConfigKeyCorsHeadersExposed, s"List[${MongoCampConfiguration.confTypeString}]")
    registerConfig(ConfigKeyCorsOriginsAllowed, s"List[${MongoCampConfiguration.confTypeString}]")

    registerConfig(ConfigKeyDocsSwagger, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)
    registerConfig(ConfigKeyOpenApi, MongoCampConfiguration.confTypeBoolean, needsRestartForActivation = true)
  }
}
