package dev.mongocamp.server.route

import dev.mongocamp.server.auth.AuthHolder
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.auth.AuthInput
import dev.mongocamp.server.model.auth.AuthorizedCollectionRequest
import dev.mongocamp.server.model.auth.UserInformation
import scala.concurrent.Future
import sttp.model.StatusCode
import sttp.tapir._

abstract class CollectionBaseRoute extends BaseRoute {

  lazy val collectionEndpoint = securedEndpointDefinition
    .securityIn(mongoDbPath)
    .securityIn("collections")
    .securityIn(path[String]("collectionName").description("The name of your MongoDb Collection"))

  lazy val readCollectionEndpoint = collectionEndpoint.serverSecurityLogic(
    connection => loginRead(connection)
  )
  lazy val writeCollectionEndpoint = collectionEndpoint.serverSecurityLogic(
    connection => loginWrite(connection)
  )
  lazy val administrateCollectionEndpoint = collectionEndpoint.serverSecurityLogic(
    connection => loginAdministrate(connection)
  )

  def loginRead(loginInformation: (AuthInput, String)): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), AuthorizedCollectionRequest]] =
    authorize(loginInformation, _.canRead(_))

  def loginWrite(loginInformation: (AuthInput, String)): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), AuthorizedCollectionRequest]] =
    authorize(loginInformation, _.canWrite(_))

  def loginAdministrate(
    loginInformation: (AuthInput, String)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), AuthorizedCollectionRequest]] =
    authorize(loginInformation, _.canAdministrate(_))

  private def authorize(
    loginInformation: (AuthInput, String),
    allowed: (UserInformation, String) => Boolean
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), AuthorizedCollectionRequest]] = {
    Future.successful {
      val userInformation = AuthHolder.findUserInformationByLoginRequest(loginInformation._1)
      if (!allowed(userInformation, loginInformation._2)) {
        throw MongoCampException.unauthorizedException("user not authorized for collection")
      }
      Right(AuthorizedCollectionRequest(userInformation, loginInformation._2))
    }
  }

}
