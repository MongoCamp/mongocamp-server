package com.quadstingray.mongo.camp.auth
import com.quadstingray.mongo.camp.config.Config
import com.quadstingray.mongo.camp.exception.MongoCampException
import com.quadstingray.mongo.camp.exception.MongoCampException.{ userNotFoundException, userOrPasswordException }
import com.quadstingray.mongo.camp.model.auth.{ UserInformation, UserRole }
import io.circe.generic.auto._
import io.circe.parser._
import sttp.model.StatusCode

class StaticAuthHolder extends AuthHolder with Config {

  private lazy val users: List[UserInformation] = {
    globalConfigStringList("auth.users")
      .map(string => decode[Option[UserInformation]](string))
      .filter(_.isRight)
      .map(_.getOrElse(None).get)
      .map(userInfo => userInfo.copy(password = encryptPassword(userInfo.password)))
  }

  private lazy val userRoles: List[UserRole] = {
    globalConfigStringList("auth.userRoles")
      .map(string => decode[Option[UserRole]](string))
      .filter(_.isRight)
      .map(_.getOrElse(None).get)
  }

  override def findUser(userId: String, password: String): UserInformation = {
    users
      .find(user => user.userId.equalsIgnoreCase(userId) && user.password.equals(password))
      .getOrElse(throw userOrPasswordException)
  }

  override def findUser(userId: String): UserInformation = {
    users.find(user => user.userId.equalsIgnoreCase(userId)).getOrElse(throw userNotFoundException)
  }

  override def findUserByApiKey(apiKey: String): UserInformation = {
    users
      .find(user => user.apiKey.equals(Option(apiKey)))
      .getOrElse(
        throw MongoCampException("apikey does not exists", StatusCode.Unauthorized)
      )
  }

  override def findUserRoles(userRoles: List[String]): List[UserRole] = {
    userRoles.flatMap(string => this.userRoles.find(_.name.equalsIgnoreCase(string)))
  }

  override def allUsers(userToSearch: Option[String]): List[UserInformation] = {
    users.filter(user => user.userId.toLowerCase().contains(userToSearch.getOrElse(user.userId).toLowerCase()))
  }

  override def allUserRoles(userRoleToSearch: Option[String]): List[UserRole] = {
    userRoles.filter(user => user.name.toLowerCase.contains(userRoleToSearch.getOrElse(user.name).toLowerCase()))
  }

}
