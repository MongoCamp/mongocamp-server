package dev.mongocamp.server.auth

import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.database.paging.{ PaginationInfo, PaginationResult }
import dev.mongocamp.server.exception.MongoCampException.userOrPasswordException
import dev.mongocamp.server.model.auth.{ Role, UserInformation }
import dev.mongocamp.server.route.parameter.paging.Paging
import dev.mongocamp.server.service.ConfigurationService
import io.circe.generic.auto._
import io.circe.parser._

class StaticAuthHolder extends AuthHolder {

  private lazy val users: List[UserInformation] = {
    ConfigurationService
      .getConfigValue[List[String]](DefaultConfigurations.ConfigKeyAuthUsers)
      .map(
        string => decode[Option[UserInformation]](string)
      )
      .filter(_.isRight)
      .map(_.getOrElse(None).get)
      .map(
        userInfo => userInfo.copy(password = encryptPassword(userInfo.password))
      )
  }

  private lazy val roles: List[Role] = {
    ConfigurationService
      .getConfigValue[List[String]](DefaultConfigurations.ConfigKeyAuthRoles)
      .map(
        string => decode[Option[Role]](string)
      )
      .filter(_.isRight)
      .map(_.getOrElse(None).get)
  }

  override def findUser(userId: String, password: String): UserInformation = {
    users
      .find(
        user => user.userId.equalsIgnoreCase(userId) && user.password.equals(password)
      )
      .getOrElse(throw userOrPasswordException)
  }

  override def findUserOption(userId: String): Option[UserInformation] = {
    users.find(
      user => user.userId.equalsIgnoreCase(userId)
    )
  }

  override def findUserByApiKeyOption(apiKey: String): Option[UserInformation] = {
    users.find(
      user => user.apiKey.equals(Option(apiKey))
    )
  }

  override def findRoles(roles: List[String]): List[Role] = {
    roles.flatMap(
      string => this.roles.find(_.name.equalsIgnoreCase(string))
    )
  }

  override def allUsers(userToSearch: Option[String], paging: Paging): (List[UserInformation], PaginationInfo) = {
    val filteredUsers = users.filter(
      user => user.userId.toLowerCase().contains(userToSearch.getOrElse(user.userId).toLowerCase())
    )
    PaginationResult.listToPaginationResult(filteredUsers, paging)
  }

  override def allRoles(roleToSearch: Option[String], paging: Paging): (List[Role], PaginationInfo) = {
    val filteredRoles = roles.filter(
      user => user.name.toLowerCase.contains(roleToSearch.getOrElse(user.name).toLowerCase())
    )
    PaginationResult.listToPaginationResult(filteredRoles, paging)
  }

}
