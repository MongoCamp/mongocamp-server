package com.quadstingray.mongo.rest.routes

import com.quadstingray.mongo.rest.config.Config
import com.quadstingray.mongo.rest.converter.CirceSchema
import com.quadstingray.mongo.rest.exception.ErrorDefinition.errorEndpointDefinition
import com.quadstingray.mongo.rest.exception.ErrorDescription
import com.quadstingray.mongo.rest.model.MongoConnection
import com.quadstingray.mongo.rest.routes.parameter.connection.ConnectionFunctions
import com.sfxcode.nosql.mongo.database.DatabaseProvider
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.SchemaDerivation

import scala.concurrent.Future

abstract class BaseRoute extends ConnectionFunctions with SchemaDerivation with Config with CirceSchema {

  implicit def convertErrorResponseToResult(error: (StatusCode, ErrorDescription)): (StatusCode, ErrorDescription, ErrorDescription) =
    (error._1, error._2, error._2)

  protected val baseEndpoint =
    endpoint.errorOut(errorEndpointDefinition)

  protected val mongoConnectionEndpoint = {
    if (globalConfigBoolean("mongorest.connection.all")) {
      endpoint.securityIn(connectionParameter).errorOut(errorEndpointDefinition).serverSecurityLogic(connection => login(connection))
    }
    else {
      val host       = globalConfigString("mongorest.connection.host")
      val port       = globalConfigInt("mongorest.connection.port")
      val database   = globalConfigString("mongorest.connection.database")
      val username   = globalConfigStringOption("mongorest.connection.username")
      val password   = globalConfigStringOption("mongorest.connection.password")
      val authdb     = globalConfigStringOption("mongorest.connection.authdb")
      val connection = MongoConnection(host, port, database, username, password, authdb)
      endpoint.errorOut(errorEndpointDefinition).serverSecurityLogic(_ => login(connection))
    }
  }

  def login(connection: MongoConnection): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), DatabaseProvider]] =
    Future.successful {
      val dbProvider = DatabaseProvider(MongoConnection.toMongoConfig(connection))
      dbProvider.collectionInfos()
      Right(dbProvider)
    }

  lazy val collectionEndpoint = mongoConnectionEndpoint.in("collections").in(path[String]("collectionName").description("The name of your MongoDb Collection"))

}
