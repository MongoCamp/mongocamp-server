package com.quadstingray.mongo.rest.database
import com.quadstingray.mongo.rest.config.Config
import com.sfxcode.nosql.mongo.database.{ DatabaseProvider, MongoConfig }

object MongoDatabase extends Config {

  lazy val databaseProvider: DatabaseProvider = {
    val host       = globalConfigString("mongorest.connection.host")
    val port       = globalConfigInt("mongorest.connection.port")
    val database   = globalConfigString("mongorest.connection.database")
    val username   = globalConfigStringOption("mongorest.connection.username")
    val password   = globalConfigStringOption("mongorest.connection.password")
    val authdb     = globalConfigStringOption("mongorest.connection.authdb")
    val connection = MongoConfig(database, host, port, "", username, password, authdb.getOrElse("admin"))
    val dbProvider = DatabaseProvider(connection)
    dbProvider
  }

}
