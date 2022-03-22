package dev.mongocamp.server.database

import com.sfxcode.nosql.mongo.bson.codecs.CustomCodecProvider
import com.sfxcode.nosql.mongo.database.{ DatabaseProvider, MongoConfig }
import dev.mongocamp.server.BuildInfo
import dev.mongocamp.server.config.Config
import dev.mongocamp.server.interceptor.RequestLogging
import dev.mongocamp.server.model.DBFileInformation
import dev.mongocamp.server.model.auth.{ Grant, Role, TokenCacheElement, UserInformation }
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

object MongoDatabase extends Config {

  lazy val collectionPrefix: String                   = globalConfigString("auth.prefix")
  private[database] lazy val CollectionNameUsers      = s"${MongoDatabase.collectionPrefix}users"
  private[database] lazy val CollectionNameRoles      = s"${MongoDatabase.collectionPrefix}roles"
  private[database] lazy val CollectionNameRequestLog = s"${MongoDatabase.collectionPrefix}request_logging"
  private[database] lazy val CollectionNameTokenCache = s"${MongoDatabase.collectionPrefix}token_cache"

  lazy val userDao: UserDao                     = UserDao()
  lazy val rolesDao: RolesDao                   = RolesDao()
  lazy val requestLoggingDao: RequestLoggingDao = RequestLoggingDao()
  lazy val tokenCacheDao: TokenCacheDao         = TokenCacheDao()

  lazy val databaseProvider: DatabaseProvider = {
    val host       = globalConfigString("connection.host")
    val port       = globalConfigInt("connection.port")
    val database   = globalConfigString("connection.database")
    val username   = globalConfigStringOption("connection.username")
    val password   = globalConfigStringOption("connection.password")
    val authdb     = globalConfigStringOption("connection.authdb")
    val connection = MongoConfig(database, host, port, s"${BuildInfo.name}/${BuildInfo.version}", username, password, authdb.getOrElse("admin"))
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
