package com.quadstingray.mongo.camp.model.auth
import com.quadstingray.mongo.camp.auth.AuthHolder
import com.quadstingray.mongo.camp.model.auth.Grant.grantTypeCollection

case class UserInformation(userId: String, password: String, apiKey: Option[String], roles: List[String]) {

  def getRoles: List[Role] = AuthHolder.handler.findRoles(this)

  def getGrants: List[Grant] = {
    val roleGrants = getRoles.flatMap(_.collectionGrants).groupBy[(String, String)](grant => (grant.name, grant.grantType))
    roleGrants
      .map(roleGroup => Grant(roleGroup._1._1, roleGroup._2.exists(_.read), roleGroup._2.exists(_.write), roleGroup._2.exists(_.administrate), roleGroup._1._2))
      .toList
  }

  def getCollectionGrants: List[Grant] = {
    val allGrands = getGrants
    allGrands.filter(_.grantType.equalsIgnoreCase(Grant.grantTypeCollection)) ++ allGrands
      .filter(grant => grant.grantType.equalsIgnoreCase(Grant.grantTypeBucketMeta) && !grant.name.equalsIgnoreCase(AuthorizedCollectionRequest.all))
      .map(grant => grant.copy(name = s"${grant.name}.files", grantType = grantTypeCollection))
  }

  def isAdmin: Boolean = {
    getRoles.exists(_.isAdmin)
  }

  def toResultUser: UserProfile = {
    UserProfile(userId, isAdmin, apiKey, roles, getGrants)
  }

}
