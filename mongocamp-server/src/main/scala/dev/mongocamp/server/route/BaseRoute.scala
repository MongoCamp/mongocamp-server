package dev.mongocamp.server.route

import dev.mongocamp.server.auth.AuthHolder
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.converter.TapirSchema
import dev.mongocamp.server.exception
import dev.mongocamp.server.exception.ErrorDefinition.errorEndpointDefinition
import dev.mongocamp.server.exception.{ ErrorDescription, MongoCampException }
import dev.mongocamp.server.model.auth._
import dev.mongocamp.server.service.ConfigurationService
import sttp.model.StatusCode
import sttp.model.headers.WWWAuthenticateChallenge
import sttp.tapir._
import sttp.tapir.generic.auto.SchemaDerivation
import sttp.tapir.model.UsernamePassword

import scala.concurrent.Future

abstract class BaseRoute extends TapirSchema with SchemaDerivation {

  implicit def convertErrorResponseToResult(error: (StatusCode, ErrorDescription)): (StatusCode, ErrorDescription, ErrorDescription) =
    (error._1, error._2, error._2)

  protected val baseEndpoint = endpoint.errorOut(errorEndpointDefinition)

  protected val securedEndpointDefinition = {
    val token = auth.apiKey(
      header[Option[String]]("X-AUTH-APIKEY").example(Some("secret1234")).description("Static API Key of the User")
    )
    val bearer              = auth.bearer[Option[String]]()
    val basicAuth           = auth.basic[Option[UsernamePassword]](WWWAuthenticateChallenge.basic("mongocamp Login"))
    val isAuthBasicEnabled  = ConfigurationService.getConfigValue[Boolean](DefaultConfigurations.ConfigKeyAuthBasic)
    val isAuthBearerEnabled = ConfigurationService.getConfigValue[Boolean](DefaultConfigurations.ConfigKeyAuthBearer)
    val isAuthTokenEnabled  = ConfigurationService.getConfigValue[Boolean](DefaultConfigurations.ConfigKeyAuthToken)

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
      throw exception.MongoCampException("not expected setting", StatusCode.InternalServerError)
    }
    baseEndpoint.securityIn(authInput)
  }

  protected val securedEndpoint = securedEndpointDefinition.serverSecurityLogic(
    loginInformation => login(loginInformation)
  )
  protected val adminEndpoint = securedEndpointDefinition.serverSecurityLogic(
    loginInformation => loginAdmin(loginInformation)
  )

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
