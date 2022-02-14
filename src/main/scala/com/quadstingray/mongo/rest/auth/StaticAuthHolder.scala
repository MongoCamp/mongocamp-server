package com.quadstingray.mongo.rest.auth
import com.quadstingray.mongo.rest.config.Config
import com.quadstingray.mongo.rest.exception.MongoRestException
import com.quadstingray.mongo.rest.exception.MongoRestException.userOrPasswordException
import com.quadstingray.mongo.rest.model.auth.{ UserInformation, UserRole, UserRoleGrant }
import io.circe.generic.auto._
import io.circe.parser._
import sttp.model.StatusCode

class StaticAuthHolder extends AuthHolder with Config {

  private lazy val users: List[UserInformation] = globalConfigStringList("mongorest.auth.users")
    .map(string => decode[Option[UserInformation]](string))
    .filter(_.isRight)
    .map(_.getOrElse(None).get)

  private lazy val userRoles: List[UserRole] = globalConfigStringList("mongorest.auth.userRoles")
    .map(string => decode[Option[UserRole]](string))
    .filter(_.isRight)
    .map(_.getOrElse(None).get)

  private lazy val userRoleGrants: List[UserRoleGrant] = globalConfigStringList("mongorest.auth.userRoleGrants")
    .map(string => decode[Option[UserRoleGrant]](string))
    .filter(_.isRight)
    .map(_.getOrElse(None).get)

  override def findUser(username: String, password: String): UserInformation = users
    .find(user => user.username.equalsIgnoreCase(username) && user.password.equals(password))
    .getOrElse(throw userOrPasswordException)

  override def findUserByApiKey(apiKey: String): UserInformation =
    users.find(user => user.apiKey.equals(Option(apiKey))).getOrElse(throw MongoRestException("apikey does not exists", StatusCode.Unauthorized))

  override def findUserRoles(userRoles: List[String]): List[UserRole] = {
    userRoles.flatMap(string => this.userRoles.find(_.name.equalsIgnoreCase(string)))
  }
  override def findUserRoleGrants(userRoleName: String): List[UserRoleGrant] = {
    this.userRoleGrants.filter(role => role.userRoleKey.equalsIgnoreCase(userRoleName))
  }

  override def updatePasswordForUser(username: String, newPassword: String): Boolean = ???
}
