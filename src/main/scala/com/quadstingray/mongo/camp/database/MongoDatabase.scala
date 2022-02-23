package com.quadstingray.mongo.camp.database
import com.quadstingray.mongo.camp.BuildInfo
import com.quadstingray.mongo.camp.config.Config
import com.quadstingray.mongo.camp.interceptor.RequestLogging
import com.quadstingray.mongo.camp.model.auth._
import com.sfxcode.nosql.mongo.bson.codecs.CustomCodecProvider
import com.sfxcode.nosql.mongo.database.{ DatabaseProvider, MongoConfig }
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

object MongoDatabase extends Config {

  lazy val collectionPrefix: String                   = globalConfigString("auth.prefix")
  private[database] lazy val CollectionNameUsers      = s"${MongoDatabase.collectionPrefix}users"
  private[database] lazy val CollectionNameUserRoles  = s"${MongoDatabase.collectionPrefix}user_roles"
  private[database] lazy val CollectionNameRequestLog = s"${MongoDatabase.collectionPrefix}request_logging"

  lazy val userDao: UserDao                     = UserDao()
  lazy val userRolesDao: UserRolesDao           = UserRolesDao()
  lazy val requestLoggingDao: RequestLoggingDao = RequestLoggingDao()

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
    classOf[UserRole],
    classOf[CollectionGrant],
    classOf[RequestLogging],
    CustomCodecProvider()
  )

}
