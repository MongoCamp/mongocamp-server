package com.quadstingray.mongo.rest.model.auth
import com.quadstingray.mongo.rest.auth.AuthHolder

case class UserRole(name: String, isAdmin: Boolean) {

  def userRoleGrants: List[UserRoleGrant] = AuthHolder.handler.findUserRoleGrants(this)

}
