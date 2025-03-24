package dev.mongocamp.server.database

import com.mongodb.event.{CommandListener, ConnectionPoolListener}
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb.database.{DatabaseProvider, MongoConfig}
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.library.BuildInfo
import dev.mongocamp.server.service.ConfigurationRead

import scala.collection.mutable.ArrayBuffer

object MongoDatabase extends LazyLogging {

  private lazy val collectionPrefix = {
    val configRead = ConfigurationRead.noPublishReader
    configRead.getConfigValue[String](DefaultConfigurations.ConfigKeyAuthPrefix)
  }
  private lazy val connectionPoolListener: ArrayBuffer[ConnectionPoolListener] = ArrayBuffer()
  private lazy val commandListener: ArrayBuffer[CommandListener]               = ArrayBuffer()

  lazy val CollectionNameConfiguration = s"${collectionPrefix}configuration"
  lazy val CollectionNameUsers         = s"${collectionPrefix}users"
  lazy val CollectionNameRoles         = s"${collectionPrefix}roles"
  lazy val CollectionNameTokenCache    = s"${collectionPrefix}token_cache"
  lazy val CollectionNameJobs          = s"${collectionPrefix}jobs"

  private var _databaseProvider: DatabaseProvider = null

  def registerConnectionPoolListener(listener: ConnectionPoolListener): Unit = {
    connectionPoolListener.+=(listener)
  }
  def registerCommandListener(listener: CommandListener): Unit = {
    commandListener.+=(listener)
  }

  def databaseProvider = {
    if (_databaseProvider == null) {
      createNewDatabaseProvider()
    }
    _databaseProvider
  }

  def createNewDatabaseProvider(): DatabaseProvider = {
    val configRead = ConfigurationRead.noPublishReader
    val connection = MongoConfig(
      configRead.getConfigValue[String](DefaultConfigurations.ConfigKeyConnectionDatabase),
      configRead.getConfigValue[String](DefaultConfigurations.ConfigKeyConnectionHost),
      configRead.getConfigValue[Long](DefaultConfigurations.ConfigKeyConnectionPort).toInt,
      s"${BuildInfo.name}/${BuildInfo.version}",
      configRead.getConfigValue[Option[String]](DefaultConfigurations.ConfigKeyConnectionUsername),
      configRead.getConfigValue[Option[String]](DefaultConfigurations.ConfigKeyConnectionPassword),
      configRead.getConfigValue[String](DefaultConfigurations.ConfigKeyConnectionAuthDb),
      connectionPoolListener = connectionPoolListener.toList,
      commandListener = commandListener.toList
    )
    logger.trace(s"Creating new DatabaseProvider with connection: $connection")
    val dbProvider = DatabaseProvider(connection)
    _databaseProvider = dbProvider
    dbProvider
  }

}
