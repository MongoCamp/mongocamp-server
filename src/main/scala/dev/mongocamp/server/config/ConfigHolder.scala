package dev.mongocamp.server.config

import java.time.Duration
import scala.util.Random

case class Configuration[A <: Any](key: String, method: String => A) {
  def value: A = method(key)
}

object ConfigHolder extends Config {
  lazy val serverInterface: Configuration[String] = Configuration("server.interface", globalConfigString)
  lazy val serverPort: Configuration[Int]         = Configuration("server.port", globalConfigInt)

  lazy val requestLogging: Configuration[Boolean] = Configuration("requestlogging.enabled", globalConfigBoolean)

  lazy val pluginsIgnored: Configuration[List[String]] = Configuration("plugins.ignored", globalConfigStringList)
  lazy val pluginsDirectory: Configuration[String]     = Configuration("plugins.directory", globalConfigString)

  lazy val authHandlerType: Configuration[String] = Configuration("auth.handler", globalConfigString)
  lazy val authSecret: Configuration[String] = {
    val loadedSecret = Configuration("auth.secret", globalConfigString)
    if (loadedSecret.value == "") {
      Configuration("auth.secret", (_: String) => Random.alphanumeric.take(32).mkString)
    }
    else {
      loadedSecret
    }
  }
  lazy val authApiKeyLength: Configuration[Int]        = Configuration("auth.apikeylength", globalConfigInt)
  lazy val authTokenCacheDb: Configuration[Boolean]    = Configuration("auth.cache.db", globalConfigBoolean)
  lazy val authTokenExpiring: Configuration[Duration]  = Configuration("auth.expiring.duration", globalConfigDuration)
  lazy val authCollectionPrefix: Configuration[String] = Configuration("auth.prefix", globalConfigString)
  lazy val authUseTypeBearer: Configuration[Boolean]   = Configuration("auth.bearer", globalConfigBoolean)
  lazy val authUseTypeBasic: Configuration[Boolean]    = Configuration("auth.basic", globalConfigBoolean)
  lazy val authUseTypeToken: Configuration[Boolean]    = Configuration("auth.token", globalConfigBoolean)

  lazy val authStaticUsers: Configuration[List[String]] = Configuration("auth.users", globalConfigStringList)
  lazy val authStaticRoles: Configuration[List[String]] = Configuration("auth.roles", globalConfigStringList)

  lazy val dbConnectionHost: Configuration[String]             = Configuration("connection.host", globalConfigString)
  lazy val dbConnectionPort: Configuration[Int]                = Configuration("connection.port", globalConfigInt)
  lazy val dbConnectionDatabase: Configuration[String]         = Configuration("connection.database", globalConfigString)
  lazy val dbConnectionUsername: Configuration[Option[String]] = Configuration("connection.username", globalConfigStringOption)
  lazy val dbConnectionPassword: Configuration[Option[String]] = Configuration("connection.password", globalConfigStringOption)
  lazy val dbConnectionAuthDb: Configuration[Option[String]]   = Configuration("connection.authdb", globalConfigStringOption)

  lazy val fileHandlerType: Configuration[String] = Configuration("file.handler", globalConfigString)
  lazy val fileCacheAge: Configuration[String]    = Configuration("file.cache.age", globalConfigString)

  lazy val corsHeadersAllowed: Configuration[List[String]] = Configuration("cors.headers.allowed", globalConfigStringList)
  lazy val corsHeadersExposed: Configuration[List[String]] = Configuration("cors.headers.exposed", globalConfigStringList)
  lazy val corsOriginsAllowed: Configuration[List[String]] = Configuration("cors.origins.allowed", globalConfigStringList)

  lazy val docsUseSwagger: Configuration[Boolean] = Configuration("docs.swagger", globalConfigBoolean)
  lazy val docsUseOpenApi: Configuration[Boolean] = Configuration("docs.openapi", globalConfigBoolean)

  // todo: search and load by reflection?!
  def allConfigurations: List[Configuration[_]] = List(
    serverInterface,
    serverPort,
    requestLogging,
    pluginsIgnored,
    pluginsDirectory,
    authHandlerType,
    authSecret,
    authApiKeyLength,
    authTokenCacheDb,
    authTokenExpiring,
    authCollectionPrefix,
    authUseTypeBearer,
    authUseTypeBasic,
    authUseTypeToken,
    authStaticUsers,
    authStaticRoles,
    dbConnectionHost,
    dbConnectionPort,
    dbConnectionDatabase,
    dbConnectionUsername,
    dbConnectionPassword,
    dbConnectionAuthDb,
    fileHandlerType,
    fileCacheAge,
    corsHeadersAllowed,
    corsHeadersExposed,
    corsOriginsAllowed,
    docsUseSwagger,
    docsUseOpenApi
  )
}
