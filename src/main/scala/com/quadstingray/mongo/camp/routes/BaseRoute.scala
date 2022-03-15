package com.quadstingray.mongo.camp.routes

import com.quadstingray.mongo.camp.auth.AuthHolder
import com.quadstingray.mongo.camp.config.Config
import com.quadstingray.mongo.camp.converter.CirceSchema
import com.quadstingray.mongo.camp.exception.ErrorDefinition.errorEndpointDefinition
import com.quadstingray.mongo.camp.exception.{ ErrorDescription, MongoCampException }
import com.quadstingray.mongo.camp.model.auth._
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

  protected val securedEndpointDefinition = {
    val token = auth.apiKey(
      header[Option[String]]("X-AUTH-APIKEY").example(Some("secret1234")).description("Static API Key of the User")
    )
    val bearer              = auth.bearer[Option[String]]()
    val basicAuth           = auth.basic[Option[UsernamePassword]](WWWAuthenticateChallenge.basic("mongocamp Login"))
    val isAuthBasicEnabled  = globalConfigBoolean("auth.basic")
    val isAuthBearerEnabled = globalConfigBoolean("auth.bearer")
    val isAuthTokenEnabled  = globalConfigBoolean("auth.token")

    val authInput = if (isAuthBearerEnabled && !isAuthBasicEnabled && !isAuthTokenEnabled) {
      bearer.mapTo[AuthInputBearer]
    }
    else if (isAuthBearerEnabled && isAuthBasicEnabled && !isAuthTokenEnabled) {
      bearer.and(basicAuth).mapTo[AuthInputBearerWithBasic]
    }
    else if (isAuthBearerEnabled && !isAuthBasicEnabled && isAuthTokenEnabled) {
      bearer.and(token).mapTo[AuthInputBearerWithApiKey]
    }
    else if (isAuthBearerEnabled && isAuthBasicEnabled && isAuthTokenEnabled) {
      bearer.and(basicAuth).and(token).mapTo[AuthInputAllMethods]
    }
    else if (!isAuthBearerEnabled && isAuthBasicEnabled && isAuthTokenEnabled) {
      basicAuth.and(token).mapTo[AuthInputBasicWithApiKey]
    }
    else if (!isAuthBearerEnabled && !isAuthBasicEnabled && isAuthTokenEnabled) {
      token.mapTo[AuthInputToken]
    }
    else if (!isAuthBearerEnabled && isAuthBasicEnabled && !isAuthTokenEnabled) {
      basicAuth.mapTo[AuthInputBasic]
    }
    else {
      throw MongoCampException("not expected setting", StatusCode.InternalServerError)
    }
    baseEndpoint.securityIn(authInput)
  }

  protected val securedEndpoint = securedEndpointDefinition.serverSecurityLogic(connection => login(connection))
  protected val adminEndpoint   = securedEndpointDefinition.serverSecurityLogic(connection => loginAdmin(connection))

  lazy val mongoDbPath = "mongodb"

  def login(loginInformation: Any): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserInformation]] = {
    Future.successful {
      val userInformation: UserInformation = AuthHolder.findUserInformationByLoginRequest(loginInformation)
      Right(userInformation)
    }
  }

  def loginAdmin(loginInformation: Any): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserInformation]] = {
    Future.successful {
      val userInformation: UserInformation = AuthHolder.findUserInformationByLoginRequest(loginInformation)
      if (userInformation.isAdmin) {
        Right(userInformation)
      }
      else {
        throw MongoCampException.unauthorizedException("user not authorized for request")
      }
    }
  }

}
