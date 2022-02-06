package com.quadstingray.mongo.rest.model
import com.sfxcode.nosql.mongo.database.MongoConfig

case class MongoConnection(host: String, port: Int, database: String, username: Option[String], password: Option[String], authDatabase: Option[String])

object MongoConnection {

  def toMongoConfig(connection: MongoConnection): MongoConfig = {
    MongoConfig(connection.database, connection.host, connection.port, "", connection.username, connection.password, connection.authDatabase.getOrElse("admin"))
  }

}
