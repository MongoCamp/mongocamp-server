package com.quadstingray.mongo.rest.model.auth

case class UserRoleGrant(userRoleKey: String, collection: String, read: Boolean, write: Boolean, administrate: Boolean) {
  def toCollectionGrant: CollectionGrant = CollectionGrant(collection, read, write, administrate)
}
