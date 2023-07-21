package dev.mongocamp.server.auth

import dev.mongocamp.server.BuildInfo
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.database.paging.PaginationInfo
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.exception.MongoCampException.{ apiKeyException, userNotFoundException }
import dev.mongocamp.server.model.auth._
import dev.mongocamp.server.route.parameter.paging.Paging
import dev.mongocamp.server.service.ConfigurationService
import io.circe.generic.auto._
import io.circe.syntax._
import org.joda.time.DateTime
import pdi.jwt.{ JwtAlgorithm, JwtCirce, JwtClaim }
import sttp.model.StatusCode

import java.security.MessageDigest
import java.time.Instant
import scala.concurrent.duration.Duration

trait AuthHolder {
  def allUsers(userToSearch: Option[String], paging: Paging): (List[UserInformation], PaginationInfo)

  def allRoles(roleToSearch: Option[String], paging: Paging): (List[Role], PaginationInfo)

  def findUserOption(userId: String): Option[UserInformation]

  def findUser(userId: String, password: String): UserInformation

  def findUserByApiKeyOption(apiKey: String): Option[UserInformation]

  def findUserByApiKey(apiKey: String): UserInformation = findUserByApiKeyOption(apiKey).getOrElse(throw apiKeyException)

  def findUser(userId: String): UserInformation = findUserOption(userId).getOrElse(throw userNotFoundException)

  def findRoles(roles: List[String]): List[Role]

  def findRole(roles: String): Option[Role] = findRoles(List(roles)).headOption

  def findRoles(userInformation: UserInformation): List[Role] = findRoles(userInformation.roles)

  def encryptPassword(password: String): String = MessageDigest.getInstance("SHA-256").digest(password.getBytes("UTF-8")).map("%02x".format(_)).mkString

  def encodeToken(userProfile: UserProfile, expirationDate: DateTime): String = {
    val claim = JwtClaim(
      expiration = Some(expirationDate.toDate.toInstant.getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond),
      issuer = Some(s"${BuildInfo.name}/${BuildInfo.version}"),
      content = userProfile.asJson.toString()
    )
    val algo  = JwtAlgorithm.HS256
    val token = JwtCirce.encode(claim, ConfigurationService.getConfigValue[String](DefaultConfigurations.ConfigKeyAuthSecret), algo)
    token
  }

  def generateLoginResult(user: UserInformation) = {
    val resultUser = user.toResultUser
    val expirationDate =
      new DateTime().plusSeconds(ConfigurationService.getConfigValue[Duration](DefaultConfigurations.ConfigKeyAuthExpiringDuration).toSeconds.toInt)
    val token = encodeToken(resultUser, expirationDate)
    TokenCache.saveToken(token, user)
    val loginResult = dev.mongocamp.server.model.auth.LoginResult(token, resultUser, expirationDate.toDate)
    loginResult
  }
}

object AuthHolder {
  private lazy val authHandlerType = ConfigurationService.getConfigValue[String](DefaultConfigurations.ConfigKeyAuthHandler)
  def isMongoDbAuthHolder: Boolean = {
    authHandlerType.equalsIgnoreCase("mongo")
  }

  def isStaticAuthHolder: Boolean = authHandlerType.equalsIgnoreCase("static")

  lazy val handler: AuthHolder = {
    authHandlerType match {
      case s: String if isStaticAuthHolder =>
        new StaticAuthHolder()
      case s: String if isMongoDbAuthHolder =>
        val mongoHolder = new MongoAuthHolder()
        mongoHolder.createIndicesAndInitData()
        mongoHolder
      case _ =>
        throw MongoCampException("Unknown Auth Handler defined", StatusCode.InternalServerError)
    }
  }

  def findUserInformationByLoginRequest(loginInformation: Any): UserInformation = {
    val userInformation = loginInformation match {
      case a: AuthInputBearer =>
        if (a.bearerToken.isEmpty) {
          throw MongoCampException.unauthorizedException()
        }
        else {
          val userInfo = TokenCache.validateToken(a.bearerToken.get).getOrElse(throw MongoCampException.unauthorizedException())
          userInfo
        }
      case a: AuthInputBearerWithApiKey =>
        if (a.bearerToken.isDefined) {
          val userInfo = TokenCache.validateToken(a.bearerToken.get).getOrElse(throw MongoCampException.unauthorizedException())
          userInfo
        }
        else if (a.apiKey.isDefined) {
          val apiKey = a.apiKey.get
          if (apiKey.trim.isEmpty || apiKey.trim.isBlank) {
            throw MongoCampException.unauthorizedException()
          }
          else {
            AuthHolder.handler.findUserByApiKey(apiKey)
          }
        }
        else {
          throw MongoCampException.unauthorizedException()
        }
      case a: AuthInputToken =>
        if (a.apiKey.isDefined) {
          val apiKey = a.apiKey.get
          if (apiKey.trim.isEmpty || apiKey.trim.isBlank) {
            throw MongoCampException.unauthorizedException()
          }
          else {
            AuthHolder.handler.findUserByApiKey(apiKey)
          }
        }
        else {
          throw MongoCampException.unauthorizedException()
        }
      case a: AuthInputBasicWithApiKey =>
        if (a.basic.isDefined) {
          AuthHolder.handler.findUser(a.basic.get.username, AuthHolder.handler.encryptPassword(a.basic.get.password.getOrElse("not_set")))
        }
        else if (a.apiKey.isDefined) {
          val apiKey = a.apiKey.get
          if (apiKey.trim.isEmpty || apiKey.trim.isBlank) {
            throw MongoCampException.unauthorizedException()
          }
          else {
            AuthHolder.handler.findUserByApiKey(apiKey)
          }
        }
        else {
          throw MongoCampException.unauthorizedException()
        }
      case a: AuthInputBasic =>
        if (a.basic.isDefined) {
          AuthHolder.handler.findUser(a.basic.get.username, AuthHolder.handler.encryptPassword(a.basic.get.password.getOrElse("not_set")))
        }
        else {
          throw MongoCampException.unauthorizedException()
        }
      case a: AuthInputAllMethods =>
        if (a.bearerToken.isDefined) {
          val userInfo = TokenCache.validateToken(a.bearerToken.get).getOrElse(throw MongoCampException.unauthorizedException())
          userInfo
        }
        else if (a.basic.isDefined) {
          AuthHolder.handler.findUser(a.basic.get.username, AuthHolder.handler.encryptPassword(a.basic.get.password.getOrElse("not_set")))
        }
        else if (a.apiKey.isDefined) {
          val apiKey = a.apiKey.get
          if (apiKey.trim.isEmpty || apiKey.trim.isBlank) {
            throw MongoCampException.unauthorizedException()
          }
          else {
            AuthHolder.handler.findUserByApiKey(apiKey)
          }
        }
        else {
          throw MongoCampException.unauthorizedException()
        }
      case a: AuthInputBearerWithBasic =>
        if (a.bearerToken.isDefined) {
          val userInfo = TokenCache.validateToken(a.bearerToken.get).getOrElse(throw MongoCampException.unauthorizedException())
          userInfo
        }
        else if (a.basic.isDefined) {
          AuthHolder.handler.findUser(a.basic.get.username, AuthHolder.handler.encryptPassword(a.basic.get.password.getOrElse("not_set")))
        }
        else {
          throw MongoCampException.unauthorizedException()
        }
      case _ => throw MongoCampException.badAuthConfiguration()
    }
    userInformation
  }
}
