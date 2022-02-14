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

  def findUser(username: String, password: String): UserInformation
  def findUserByApiKey(apiKey: String): UserInformation

  def findUserRoleGrants(userRoleName: String): List[UserRoleGrant]
  def findUserRoleGrants(userRole: UserRole): List[UserRoleGrant] = findUserRoleGrants(userRole.name)

  def findUserRoles(userRoles: List[String]): List[UserRole]
  def findUserRole(userRole: String): Option[UserRole]                = findUserRoles(List(userRole)).headOption
  def findUserRoles(userInformation: UserInformation): List[UserRole] = findUserRoles(userInformation.userRoles)

  def updatePasswordForUser(username: String, newPassword: String): Boolean

  def encryptPassword(password: String): String = MessageDigest.getInstance("SHA-256").digest(password.getBytes("UTF-8")).map("%02x".format(_)).mkString

  def encodeToken(userProfile: UserProfile, expirationDate: DateTime): String = {
    val claim = JwtClaim(
      expiration = Some(expirationDate.toDate.toInstant.getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond),
      issuer = Some("%s/%s".format(BuildInfo.name, BuildInfo.version)),
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
  lazy val handler: AuthHolder = {
    globalConfigString("mongorest.auth.handler") match {
      case s: String if s.equalsIgnoreCase("static") => new StaticAuthHolder()
      case s: String if s.equalsIgnoreCase("mongo")  => new StaticAuthHolder()
      case _                                         => throw MongoRestException("Unknown Auth Handler defined", StatusCode.InternalServerError)
    }
  }

  lazy val secret: String = globalConfigString("mongorest.auth.secret")

  lazy val expiringDuration: Duration = configDuration("mongorest.auth.expiringDuration")

  lazy val tokenCache =
    Scaffeine().recordStats().expireAfterWrite(FiniteDuration(expiringDuration.getSeconds, TimeUnit.SECONDS)).build[String, UserInformation]()

}
