package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb.bson.codecs.CustomCodecProvider
import dev.mongocamp.driver.mongodb.database.{DatabaseProvider, MongoConfig}
import dev.mongocamp.server.BuildInfo
import dev.mongocamp.server.config.{ConfigManager, DefaultConfigurations}
import dev.mongocamp.server.interceptor.RequestLogging
import dev.mongocamp.server.model.auth.{Grant, Role, TokenCacheElement, UserInformation}
import dev.mongocamp.server.model.{DBFileInformation, JobConfig}
import dev.mongocamp.server.monitoring.MetricsConfiguration
import io.micrometer.core.instrument.binder.mongodb.{MongoMetricsCommandListener, MongoMetricsConnectionPoolListener}
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

object MongoDatabase {

  private lazy val collectionPrefix = ConfigManager.getConfigValue[String](DefaultConfigurations.ConfigKeyAuthPrefix)

  private[database] lazy val CollectionNameConfiguration = s"${collectionPrefix}configuration"
  private[database] lazy val CollectionNameUsers         = s"${collectionPrefix}users"
  private[database] lazy val CollectionNameRoles         = s"${collectionPrefix}roles"
  private[database] lazy val CollectionNameRequestLog    = s"${collectionPrefix}request_logging"
  private[database] lazy val CollectionNameTokenCache    = s"${collectionPrefix}token_cache"
  private[database] lazy val CollectionNameJobs          = s"${collectionPrefix}jobs"

  lazy val userDao: UserDao                     = UserDao()
  lazy val rolesDao: RolesDao                   = RolesDao()
  lazy val requestLoggingDao: RequestLoggingDao = RequestLoggingDao()
  lazy val tokenCacheDao: TokenCacheDao         = TokenCacheDao()
  lazy val jobDao: JobDao                       = JobDao()

  lazy val databaseProvider: DatabaseProvider = {
    val connection = MongoConfig(
      ConfigManager.getConfigValue[String](DefaultConfigurations.ConfigKeyConnectionDatabase),
      ConfigManager.getConfigValue[String](DefaultConfigurations.ConfigKeyConnectionHost),
      ConfigManager.getConfigValue[Long](DefaultConfigurations.ConfigKeyConnectionPort).toInt,
      s"${BuildInfo.name}/${BuildInfo.version}",
      ConfigManager.getConfigValue[Option[String]](DefaultConfigurations.ConfigKeyConnectionUsername),
      ConfigManager.getConfigValue[Option[String]](DefaultConfigurations.ConfigKeyConnectionPassword),
      ConfigManager.getConfigValue[String](DefaultConfigurations.ConfigKeyConnectionAuthDb),
      connectionPoolListener = List(new MongoMetricsConnectionPoolListener(MetricsConfiguration.mongoDbRegistry)),
      commandListener = List(new MongoMetricsCommandListener(MetricsConfiguration.mongoDbRegistry))
    )
    val dbProvider = DatabaseProvider(connection, fromRegistries(DEFAULT_CODEC_REGISTRY, providerRegistry))
    dbProvider
  }

  private val providerRegistry = fromProviders(
    classOf[UserInformation],
    classOf[Role],
    classOf[Grant],
    classOf[RequestLogging],
    classOf[TokenCacheElement],
    classOf[DBFileInformation],
    classOf[JobConfig],
    CustomCodecProvider()
  )

}
