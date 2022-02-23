package com.quadstingray.mongo.camp.auth
import com.quadstingray.mongo.camp.config.Config
import com.quadstingray.mongo.camp.database.paging.{ PaginationInfo, PaginationResult }
import com.quadstingray.mongo.camp.exception.MongoCampException.userOrPasswordException
import com.quadstingray.mongo.camp.model.auth.{ UserInformation, UserRole }
import com.quadstingray.mongo.camp.routes.parameter.paging.Paging
import io.circe.generic.auto._
import io.circe.parser._

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

  override def findUserOption(userId: String): Option[UserInformation] = {
    users.find(user => user.userId.equalsIgnoreCase(userId))
  }

  override def findUserByApiKeyOption(apiKey: String): Option[UserInformation] = {
    users.find(user => user.apiKey.equals(Option(apiKey)))
  }

  override def findUserRoles(userRoles: List[String]): List[UserRole] = {
    userRoles.flatMap(string => this.userRoles.find(_.name.equalsIgnoreCase(string)))
  }

  override def allUsers(userToSearch: Option[String], paging: Paging): (List[UserInformation], PaginationInfo) = {
    val filteredUsers = users.filter(user => user.userId.toLowerCase().contains(userToSearch.getOrElse(user.userId).toLowerCase()))
    PaginationResult.listToPaginationResult(filteredUsers, paging)
  }

  override def allUserRoles(userRoleToSearch: Option[String], paging: Paging): (List[UserRole], PaginationInfo) = {
    val filteredUserRoles = userRoles.filter(user => user.name.toLowerCase.contains(userRoleToSearch.getOrElse(user.name).toLowerCase()))
    PaginationResult.listToPaginationResult(filteredUserRoles, paging)
  }

}
