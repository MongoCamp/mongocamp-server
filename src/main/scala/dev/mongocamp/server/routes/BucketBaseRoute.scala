package dev.mongocamp.server.routes

import dev.mongocamp.server.auth.AuthHolder
import dev.mongocamp.server.exception.{ ErrorDescription, MongoCampException }
import dev.mongocamp.server.model.auth.{ AuthInput, AuthorizedCollectionRequest, UserInformation }
import sttp.model.StatusCode
import sttp.tapir._

import scala.concurrent.Future

abstract class BucketBaseRoute extends BaseRoute {

  lazy val bucketBaseEndpoint = securedEndpointDefinition
    .securityIn(mongoDbPath)
    .securityIn("buckets")
    .securityIn(path[String]("bucketName").description("The name of your MongoDb Collection"))

  lazy val readBucketEndpoint         = bucketBaseEndpoint.serverSecurityLogic(connection => loginRead(connection))
  lazy val writeBucketEndpoint        = bucketBaseEndpoint.serverSecurityLogic(connection => loginWrite(connection))
  lazy val administrateBucketEndpoint = bucketBaseEndpoint.serverSecurityLogic(connection => loginAdministrate(connection))

  def loginRead(loginInformation: (AuthInput, String)): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), AuthorizedCollectionRequest]] = {
    Future.successful {
      val userInformation: UserInformation = AuthHolder.findUserInformationByLoginRequest(loginInformation._1)
      if (userInformation.isAdmin) {
        Right(AuthorizedCollectionRequest(userInformation, loginInformation._2))
      }
      else {
        userInformation.getBucketGrants
          .find(bucketGrant => {
            val bucket        = bucketGrant.name
            val isBucketGrant = bucket.equalsIgnoreCase(loginInformation._2) || bucket.equalsIgnoreCase(AuthorizedCollectionRequest.all)
            isBucketGrant && bucketGrant.read
          })
          .getOrElse(throw MongoCampException.unauthorizedException("user not authorized for bucket"))
        Right(AuthorizedCollectionRequest(userInformation, loginInformation._2))
      }
    }
  }

  def loginWrite(loginInformation: (AuthInput, String)): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), AuthorizedCollectionRequest]] = {
    Future.successful {
      val userInformation: UserInformation = AuthHolder.findUserInformationByLoginRequest(loginInformation._1)
      if (userInformation.isAdmin) {
        Right(AuthorizedCollectionRequest(userInformation, loginInformation._2))
      }
      else {
        userInformation.getBucketGrants
          .find(bucketGrant => {
            val bucket        = bucketGrant.name
            val isBucketGrant = bucket.equalsIgnoreCase(loginInformation._2) || bucket.equalsIgnoreCase(AuthorizedCollectionRequest.all)
            isBucketGrant && bucketGrant.write
          })
          .getOrElse(throw MongoCampException.unauthorizedException("user not authorized for bucket"))
        Right(AuthorizedCollectionRequest(userInformation, loginInformation._2))
      }
    }
  }

  def loginAdministrate(
      loginInformation: (AuthInput, String)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), AuthorizedCollectionRequest]] = {
    Future.successful {
      val userInformation: UserInformation = AuthHolder.findUserInformationByLoginRequest(loginInformation._1)
      if (userInformation.isAdmin) {
        Right(AuthorizedCollectionRequest(userInformation, loginInformation._2))
      }
      else {
        userInformation.getBucketGrants
          .find(bucketGrant => {
            val bucket        = bucketGrant.name
            val isBucketGrant = bucket.equalsIgnoreCase(loginInformation._2) || bucket.equalsIgnoreCase(AuthorizedCollectionRequest.all)
            isBucketGrant && bucketGrant.administrate
          })
          .getOrElse(throw MongoCampException.unauthorizedException("user not authorized for bucket"))
        Right(AuthorizedCollectionRequest(userInformation, loginInformation._2))
      }
    }
  }

}
