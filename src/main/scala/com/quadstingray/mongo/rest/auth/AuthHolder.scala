package com.quadstingray.mongo.rest.auth
import com.github.blemale.scaffeine.Scaffeine
import com.quadstingray.mongo.rest.BuildInfo
import com.quadstingray.mongo.rest.auth.AuthHolder.expiringDuration
import com.quadstingray.mongo.rest.config.Config
import com.quadstingray.mongo.rest.exception.MongoRestException
import com.quadstingray.mongo.rest.model.auth._
import io.circe.generic.auto._
import io.circe.syntax._
import org.joda.time.DateTime
import pdi.jwt.{ JwtAlgorithm, JwtCirce, JwtClaim }
import sttp.model.StatusCode

import java.security.MessageDigest
import java.time.{ Duration, Instant }
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

trait AuthHolder {
  def allUsers(userToSearch: Option[String]): List[UserInformation]
  def allUserRoles(userRoleToSearch: Option[String]): List[UserRole]

  def findUser(userId: String): UserInformation
  def findUser(userId: String, password: String): UserInformation
  def findUserByApiKey(apiKey: String): UserInformation

  def findUserRoleGrants(userRoleName: String): List[UserRoleGrant]
  def findUserRoleGrants(userRole: UserRole): List[UserRoleGrant] = findUserRoleGrants(userRole.name)

  def findUserRoles(userRoles: List[String]): List[UserRole]
  def findUserRole(userRole: String): Option[UserRole]                = findUserRoles(List(userRole)).headOption
  def findUserRoles(userInformation: UserInformation): List[UserRole] = findUserRoles(userInformation.userRoles)

  def encryptPassword(password: String): String = MessageDigest.getInstance("SHA-256").digest(password.getBytes("UTF-8")).map("%02x".format(_)).mkString

  def encodeToken(userProfile: UserProfile, expirationDate: DateTime): String = {
    val claim = JwtClaim(
      expiration = Some(expirationDate.toDate.toInstant.getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond),
      issuer = Some(s"${BuildInfo.name}/${BuildInfo.version}"),
      content = userProfile.asJson.toString()
    )
    val algo  = JwtAlgorithm.HS256
    val token = JwtCirce.encode(claim, AuthHolder.secret, algo)
    token
  }

  def generateLoginResult(user: UserInformation) = {
    val resultUser     = user.toResultUser
    val expirationDate = new DateTime().plusSeconds(expiringDuration.toSeconds.toInt)
    val token          = encodeToken(resultUser, expirationDate)
    AuthHolder.tokenCache.put(token, user)
    val loginResult = LoginResult(token, resultUser, expirationDate.toDate)
    loginResult
  }
}

object AuthHolder extends Config {
  lazy val authHolderType: String = globalConfigString("mongorest.auth.handler")

  def isMongoDbAuthHolder: Boolean = authHolderType.equalsIgnoreCase("mongo")
  def isStaticAuthHolder: Boolean  = authHolderType.equalsIgnoreCase("static")

  lazy val handler: AuthHolder = {
    authHolderType match {
      case s: String if isStaticAuthHolder =>
        new StaticAuthHolder()
      case s: String if isMongoDbAuthHolder =>
        val mongoHolder = new MongoAuthHolder()
        mongoHolder.createIndicesAndInitData()
        mongoHolder
      case _ =>
        throw MongoRestException("Unknown Auth Handler defined", StatusCode.InternalServerError)
    }
  }

  lazy val secret: String = globalConfigString("mongorest.auth.secret")

  lazy val expiringDuration: Duration = globalConfigDuration("mongorest.auth.expiringDuration")
  lazy val apiKeyLength: Int          = globalConfigInt("mongorest.auth.apikeylength")

  lazy val tokenCache =
    Scaffeine().recordStats().expireAfterWrite(FiniteDuration(expiringDuration.getSeconds, TimeUnit.SECONDS)).build[String, UserInformation]()

  def findUserInformationByLoginRequest(loginInformation: Any): UserInformation = {
    val userInformation = loginInformation match {
      case a: AuthInputAllMethods => throw MongoRestException.badAuthConfiguration() // todo: https://github.com/softwaremill/tapir/issues/1845
      case a: AuthInputBearer =>
        if (a.bearerToken.isEmpty) {
          throw MongoRestException.unauthorizedException()
        }
        else {
          val userInfo = AuthHolder.tokenCache.getIfPresent(a.bearerToken.get).getOrElse(throw MongoRestException.unauthorizedException())
          userInfo
        }

      case a: AuthInputWithBasic => throw MongoRestException.badAuthConfiguration() // todo: https://github.com/softwaremill/tapir/issues/1845
      case a: AuthInputWithApiKey =>
        if (a.bearerToken.isDefined) {
          val userInfo = AuthHolder.tokenCache.getIfPresent(a.bearerToken.get).getOrElse(throw MongoRestException.unauthorizedException())
          userInfo
        }
        else if (a.apiKey.isDefined) {
          val apiKey = a.apiKey.get
          if (apiKey.trim.isEmpty || apiKey.trim.isBlank) {
            throw MongoRestException.unauthorizedException()
          }
          else {
            AuthHolder.handler.findUserByApiKey(apiKey)
          }
        }
        else {
          throw MongoRestException.unauthorizedException()
        }

      case _ => throw MongoRestException.badAuthConfiguration()
    }
    userInformation
  }
}
