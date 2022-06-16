package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb.bson.codecs.CustomCodecProvider
import dev.mongocamp.driver.mongodb.database.{ DatabaseProvider, MongoConfig }
import dev.mongocamp.server.BuildInfo
import dev.mongocamp.server.config.ConfigHolder
import dev.mongocamp.server.config.ConfigHolder._
import dev.mongocamp.server.interceptor.RequestLogging
import dev.mongocamp.server.model.DBFileInformation
import dev.mongocamp.server.model.auth.{ Grant, Role, TokenCacheElement, UserInformation }
import dev.mongocamp.server.monitoring.MetricsConfiguration
import io.micrometer.core.instrument.binder.mongodb.{ MongoMetricsCommandListener, MongoMetricsConnectionPoolListener }
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

object MongoDatabase {

  private[database] lazy val CollectionNameUsers      = s"${ConfigHolder.authCollectionPrefix.value}users"
  private[database] lazy val CollectionNameRoles      = s"${ConfigHolder.authCollectionPrefix.value}roles"
  private[database] lazy val CollectionNameRequestLog = s"${ConfigHolder.authCollectionPrefix.value}request_logging"
  private[database] lazy val CollectionNameTokenCache = s"${ConfigHolder.authCollectionPrefix.value}token_cache"

  lazy val userDao: UserDao                     = UserDao()
  lazy val rolesDao: RolesDao                   = RolesDao()
  lazy val requestLoggingDao: RequestLoggingDao = RequestLoggingDao()
  lazy val tokenCacheDao: TokenCacheDao         = TokenCacheDao()

  lazy val databaseProvider: DatabaseProvider = {
    val connection = MongoConfig(
      dbConnectionDatabase.value,
      dbConnectionHost.value,
      dbConnectionPort.value,
      s"${BuildInfo.name}/${BuildInfo.version}",
      dbConnectionUsername.value,
      dbConnectionPassword.value,
      dbConnectionAuthDb.value,
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
    CustomCodecProvider()
  )

}
