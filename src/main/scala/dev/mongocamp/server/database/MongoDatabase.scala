package dev.mongocamp.server.database

import com.mongodb.event.{CommandListener, ConnectionPoolListener}
import dev.mongocamp.driver.mongodb.bson.codecs.CustomCodecProvider
import dev.mongocamp.driver.mongodb.database.{DatabaseProvider, MongoConfig}
import dev.mongocamp.server.BuildInfo
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.event.listener.DatabaseRequestLoggingElement
import dev.mongocamp.server.model.auth.{Grant, Role, TokenCacheElement, UserInformation}
import dev.mongocamp.server.model.{DBFileInformation, JobConfig}
import dev.mongocamp.server.service.ConfigurationService
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

object MongoDatabase {

  private lazy val collectionPrefix = ConfigurationService.getConfigValue[String](DefaultConfigurations.ConfigKeyAuthPrefix)
  private lazy val connectionPoolListener: ArrayBuffer[ConnectionPoolListener] = ArrayBuffer()
  private lazy val commandListener: ArrayBuffer[CommandListener]               = ArrayBuffer()

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

  private var _databaseProvider: DatabaseProvider = null

  def registerConnectionPoolListener(listener: ConnectionPoolListener): Unit = {
    connectionPoolListener.addOne(listener)
  }
  def registerCommandListener(listener: CommandListener): Unit = {
    commandListener.addOne(listener)
  }

  def databaseProvider = {
    if (_databaseProvider == null) {
      createNewDatabaseProvider()
    }
    _databaseProvider
  }

  def createNewDatabaseProvider(): DatabaseProvider = {
    val connection = MongoConfig(
      ConfigurationService.getConfigValue[String](DefaultConfigurations.ConfigKeyConnectionDatabase),
      ConfigurationService.getConfigValue[String](DefaultConfigurations.ConfigKeyConnectionHost),
      ConfigurationService.getConfigValue[Long](DefaultConfigurations.ConfigKeyConnectionPort).toInt,
      s"${BuildInfo.name}/${BuildInfo.version}",
      ConfigurationService.getConfigValue[Option[String]](DefaultConfigurations.ConfigKeyConnectionUsername),
      ConfigurationService.getConfigValue[Option[String]](DefaultConfigurations.ConfigKeyConnectionPassword),
      ConfigurationService.getConfigValue[String](DefaultConfigurations.ConfigKeyConnectionAuthDb),
      connectionPoolListener = connectionPoolListener.toList,
      commandListener = commandListener.toList
    )
    val dbProvider = DatabaseProvider(connection, fromRegistries(DEFAULT_CODEC_REGISTRY, providerRegistry))
    _databaseProvider = dbProvider
    dbProvider
  }

  val userProviders: ArrayBuffer[CodecProvider] = ArrayBuffer(
    classOf[UserInformation],
    classOf[Role],
    classOf[Grant],
    classOf[DatabaseRequestLoggingElement],
    classOf[TokenCacheElement],
    classOf[DBFileInformation],
    classOf[JobConfig],CustomCodecProvider()
  )


  def addToProvider(provider: CodecProvider): Unit = userProviders += provider

  private lazy val providerRegistry = fromProviders(userProviders.asJava)

}
