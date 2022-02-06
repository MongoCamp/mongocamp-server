package com.quadstingray.mongo.rest.routes.parameter.connection

import com.quadstingray.mongo.rest.model.MongoConnection
import com.quadstingray.mongo.rest.routes.parameter.connection.ConnectionFunctions._
import sttp.tapir.header

trait ConnectionFunctions {

  val connectionParameter = header[String](HeaderMongoDbHost)
    .description("Host path of your MongoDb")
    .and(header[Int](HeaderMongoDbPort).default(27017).description("Port of your MongoDb (default: 27017)"))
    .and(header[String](HeaderMongoDbDatabase).description("Default Database to connect to your MongoDb"))
    .and(header[Option[String]](HeaderMongoDbUsername).description("Username to connect to your MongoDb"))
    .and(header[Option[String]](HeaderMongoDbPassword).description("Password to connect to your MongoDb"))
    .and(header[Option[String]](HeaderMongoDbAuthDb).description("AuthDb to connect to your MongoDb"))
    .mapTo[MongoConnection]

}

object ConnectionFunctions {
  final lazy val HeaderMongoDbHost     = "x-mongo-host"
  final lazy val HeaderMongoDbPort     = "x-mongo-port"
  final lazy val HeaderMongoDbDatabase = "x-mongo-database"
  final lazy val HeaderMongoDbUsername = "x-mongo-username"
  final lazy val HeaderMongoDbPassword = "x-mongo-password"
  final lazy val HeaderMongoDbAuthDb   = "x-mongo-authdb"
}
