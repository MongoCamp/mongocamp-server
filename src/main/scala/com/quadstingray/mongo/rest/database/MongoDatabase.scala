package com.quadstingray.mongo.rest.database
import com.quadstingray.mongo.rest.BuildInfo
import com.quadstingray.mongo.rest.config.Config
import com.quadstingray.mongo.rest.model.auth._
import com.sfxcode.nosql.mongo.bson.codecs.CustomCodecProvider
import com.sfxcode.nosql.mongo.database.{ DatabaseProvider, MongoConfig }
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

object MongoDatabase extends Config {

  lazy val collectionPrefix: String                   = globalConfigString("mongorest.auth.prefix")
  private[database] lazy val CollectionNameUsers      = s"${MongoDatabase.collectionPrefix}users"
  private[database] lazy val CollectionNameUserRoles  = s"${MongoDatabase.collectionPrefix}user_roles"
  private[database] lazy val CollectionNameRoleGrants = s"${MongoDatabase.collectionPrefix}role_grants"

  lazy val userDao: UserDao             = UserDao()
  lazy val userRolesDao: UserRolesDao   = UserRolesDao()
  lazy val roleGrantsDao: RoleGrantsDao = RoleGrantsDao()

  lazy val databaseProvider: DatabaseProvider = {
    val host       = globalConfigString("mongorest.connection.host")
    val port       = globalConfigInt("mongorest.connection.port")
    val database   = globalConfigString("mongorest.connection.database")
    val username   = globalConfigStringOption("mongorest.connection.username")
    val password   = globalConfigStringOption("mongorest.connection.password")
    val authdb     = globalConfigStringOption("mongorest.connection.authdb")
    val connection = MongoConfig(database, host, port, s"${BuildInfo.name}/${BuildInfo.version}", username, password, authdb.getOrElse("admin"))
    val dbProvider = DatabaseProvider(connection, fromRegistries(DEFAULT_CODEC_REGISTRY, providerRegistry))
    dbProvider
  }

  private val providerRegistry = fromProviders(
    classOf[UserInformation],
    classOf[UserRole],
    classOf[UserRoleGrant],
    CustomCodecProvider()
  )

}
