package dev.mongocamp.server.model.auth

import dev.mongocamp.server.auth.AuthHolder
import dev.mongocamp.server.model.BucketInformation.BucketCollectionSuffix
import dev.mongocamp.server.model.ModelConstants

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
    allGrands.filter(_.grantType.equalsIgnoreCase(ModelConstants.grantTypeCollection)) ++ allGrands
      .filter(grant => grant.grantType.equalsIgnoreCase(ModelConstants.grantTypeBucketMeta))
      .map(grant => grant.copy(name = s"${grant.name}$BucketCollectionSuffix"))
  }

  def getBucketGrants: List[Grant] = {
    getGrants.filter(grant =>
      grant.grantType.equalsIgnoreCase(ModelConstants.grantTypeBucketMeta) || grant.grantType.equalsIgnoreCase(ModelConstants.grantTypeBucket)
    )
  }

  def isAdmin: Boolean = {
    getRoles.exists(_.isAdmin)
  }

  def toResultUser: UserProfile = {
    UserProfile(userId, isAdmin, apiKey, roles, getGrants)
  }

}
