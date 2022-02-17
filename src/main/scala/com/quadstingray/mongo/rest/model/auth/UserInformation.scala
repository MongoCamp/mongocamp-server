package com.quadstingray.mongo.rest.model.auth
import com.quadstingray.mongo.rest.auth.AuthHolder

case class UserInformation(userId: String, password: String, apiKey: Option[String], userRoles: List[String]) {

  def getUserRoles: List[UserRole] = AuthHolder.handler.findUserRoles(this)

  def getCollectionGrants: List[UserRoleGrant] = {
    val roleGrants = getUserRoles.flatMap(_.userRoleGrants).groupBy[String](_.collection)
    roleGrants
      .map(roleGroup =>
        UserRoleGrant("collectionMapped", roleGroup._1, roleGroup._2.exists(_.read), roleGroup._2.exists(_.write), roleGroup._2.exists(_.administrate))
      )
      .toList
  }

  def isAdmin: Boolean = {
    getUserRoles.exists(_.isAdmin)
  }

  def toResultUser: UserProfile = {
    UserProfile(userId, isAdmin, userRoles, getCollectionGrants.map(_.toCollectionGrant))
  }

}
