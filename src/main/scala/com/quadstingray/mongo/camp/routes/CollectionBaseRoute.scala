package com.quadstingray.mongo.camp.routes

import com.quadstingray.mongo.camp.auth.AuthHolder
import com.quadstingray.mongo.camp.exception.{ ErrorDescription, MongoCampException }
import com.quadstingray.mongo.camp.model.BucketInformation.BucketCollectionSuffix
import com.quadstingray.mongo.camp.model.auth._
import sttp.model.StatusCode
import sttp.tapir._

import scala.concurrent.Future

abstract class CollectionBaseRoute extends BaseRoute {

  lazy val collectionEndpoint = securedEndpointDefinition
    .securityIn(mongoDbPath)
    .securityIn("collections")
    .securityIn(path[String]("collectionName").description("The name of your MongoDb Collection"))

  lazy val readCollectionEndpoint         = collectionEndpoint.serverSecurityLogic(connection => loginRead(connection))
  lazy val writeCollectionEndpoint        = collectionEndpoint.serverSecurityLogic(connection => loginWrite(connection))
  lazy val administrateCollectionEndpoint = collectionEndpoint.serverSecurityLogic(connection => loginAdministrate(connection))

  def loginRead(loginInformation: (AuthInput, String)): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), AuthorizedCollectionRequest]] = {
    Future.successful {
      val userInformation: UserInformation = AuthHolder.findUserInformationByLoginRequest(loginInformation._1)
      if (userInformation.isAdmin) {
        Right(AuthorizedCollectionRequest(userInformation, loginInformation._2))
      }
      else {
        userInformation.getCollectionGrants
          .find(collectionGrant => {
            val collection        = collectionGrant.name
            val isCollectionGrant = collection.equalsIgnoreCase(loginInformation._2) || collection.equalsIgnoreCase(AuthorizedCollectionRequest.all)
            val isBucketMetaGrant = s"${AuthorizedCollectionRequest.all}$BucketCollectionSuffix".equalsIgnoreCase(loginInformation._2) && loginInformation._2
              .endsWith(BucketCollectionSuffix)
            (isCollectionGrant || isBucketMetaGrant) && collectionGrant.read
          })
          .getOrElse(throw MongoCampException.unauthorizedException("user not authorized for collection"))
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
        userInformation.getCollectionGrants
          .find(collectionGrant => {
            val collection        = collectionGrant.name
            val isCollectionGrant = collection.equalsIgnoreCase(loginInformation._2) || collection.equalsIgnoreCase(AuthorizedCollectionRequest.all)
            val isBucketMetaGrant = s"${AuthorizedCollectionRequest.all}$BucketCollectionSuffix".equalsIgnoreCase(loginInformation._2) && loginInformation._2
              .endsWith(BucketCollectionSuffix)
            (isCollectionGrant || isBucketMetaGrant) && collectionGrant.write
          })
          .getOrElse(throw MongoCampException.unauthorizedException("user not authorized for collection"))
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
        userInformation.getCollectionGrants
          .find(collectionGrant => {
            val collection        = collectionGrant.name
            val isCollectionGrant = collection.equalsIgnoreCase(loginInformation._2) || collection.equalsIgnoreCase(AuthorizedCollectionRequest.all)
            val isBucketMetaGrant = s"${AuthorizedCollectionRequest.all}$BucketCollectionSuffix".equalsIgnoreCase(loginInformation._2) && loginInformation._2
              .endsWith(BucketCollectionSuffix)
            (isCollectionGrant || isBucketMetaGrant) && collectionGrant.administrate
          })
          .getOrElse(throw MongoCampException.unauthorizedException("user not authorized for collection"))
        Right(AuthorizedCollectionRequest(userInformation, loginInformation._2))
      }
    }
  }

}
