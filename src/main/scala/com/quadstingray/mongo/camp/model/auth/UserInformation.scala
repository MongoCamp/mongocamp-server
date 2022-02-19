package com.quadstingray.mongo.camp.model.auth
import com.quadstingray.mongo.camp.auth.AuthHolder

case class UserInformation(userId: String, password: String, apiKey: Option[String], userRoles: List[String]) {

  def getUserRoles: List[UserRole] = AuthHolder.handler.findUserRoles(this)

  def getCollectionGrants: List[CollectionGrant] = {
    val roleGrants = getUserRoles.flatMap(_.collectionGrants).groupBy[String](_.collection)
    roleGrants
      .map(roleGroup => CollectionGrant(roleGroup._1, roleGroup._2.exists(_.read), roleGroup._2.exists(_.write), roleGroup._2.exists(_.administrate)))
      .toList
  }

  def isAdmin: Boolean = {
    getUserRoles.exists(_.isAdmin)
  }

  def toResultUser: UserProfile = {
    UserProfile(userId, isAdmin, apiKey, userRoles, getCollectionGrants)
  }

}
