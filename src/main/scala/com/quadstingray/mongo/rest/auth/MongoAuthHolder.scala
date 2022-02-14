package com.quadstingray.mongo.rest.auth
import com.quadstingray.mongo.rest.model.auth.{ UserInformation, UserRole, UserRoleGrant }

class MongoAuthHolder extends AuthHolder {
  override def findUser(username: String, password: String): UserInformation         = ???
  override def findUserRoles(userRoles: List[String]): List[UserRole]                = ???
  override def findUserRoleGrants(userRoleName: String): List[UserRoleGrant]         = ???
  override def updatePasswordForUser(username: String, newPassword: String): Boolean = ???
  override def findUserByApiKey(apiKey: String): UserInformation                     = ???
}
