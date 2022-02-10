package com.quadstingray.mongo.rest.routes

import com.quadstingray.mongo.rest.auth.AuthHolder
import com.quadstingray.mongo.rest.config.Config
import com.quadstingray.mongo.rest.converter.CirceSchema
import com.quadstingray.mongo.rest.exception.ErrorDefinition.errorEndpointDefinition
import com.quadstingray.mongo.rest.exception.{ ErrorDescription, MongoRestException }
import com.quadstingray.mongo.rest.model.auth._
import sttp.model.StatusCode
import sttp.model.headers.WWWAuthenticateChallenge
import sttp.tapir._
import sttp.tapir.generic.SchemaDerivation
import sttp.tapir.model.UsernamePassword

import scala.concurrent.Future

abstract class BaseRoute extends Config with CirceSchema with SchemaDerivation {

  implicit def convertErrorResponseToResult(error: (StatusCode, ErrorDescription)): (StatusCode, ErrorDescription, ErrorDescription) =
    (error._1, error._2, error._2)

  protected val baseEndpoint = endpoint.errorOut(errorEndpointDefinition)

  protected val mongoConnectionEndpoint = {
    val token = auth.apiKey(
      header[Option[String]]("X-AUTH-APIKEY").example(Some("secret1234")).description("Static API Key of the User")
    )
    val bearer             = auth.bearer[Option[String]]()
    val basicAuth          = auth.basic[Option[UsernamePassword]](WWWAuthenticateChallenge.basic("MongoRest Login"))
    val isAuthBasicEnabled = globalConfigBoolean("mongorest.auth.basic")
    val isAuthTokenEnabled = globalConfigBoolean("mongorest.auth.token")

    val authInput = if (!isAuthBasicEnabled && !isAuthTokenEnabled) {
      bearer.mapTo[AuthInputBearer]
    }
    else if (isAuthBasicEnabled && !isAuthTokenEnabled) {
      bearer.and(basicAuth).mapTo[AuthInputWithBasic]
    }
    else if (!isAuthBasicEnabled && isAuthTokenEnabled) {
      bearer.and(token).mapTo[AuthInputWithToken]
    }
    else if (isAuthBasicEnabled && isAuthTokenEnabled) {
      bearer.and(basicAuth).and(token).mapTo[AuthInputAllMethods]
    }
    else {
      throw MongoRestException("not expected setting", StatusCode.InternalServerError)
    }
    baseEndpoint.securityIn(authInput).serverSecurityLogic(connection => login(connection))
  }

  def login(loginInformation: Any): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserInformation]] =
    Future.successful {
      Right(AuthHolder.handler.findUser("test", AuthHolder.handler.encryptPassword("test1234")))
    }

  lazy val collectionEndpoint = mongoConnectionEndpoint.in("collections").in(path[String]("collectionName").description("The name of your MongoDb Collection"))

}
