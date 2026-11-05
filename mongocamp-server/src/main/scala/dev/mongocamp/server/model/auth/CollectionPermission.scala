package dev.mongocamp.server.model.auth

/** What the logged in user may do with one collection, resolved by the server so that clients do not have to re-implement the grant matching. */
case class CollectionPermission(name: String, read: Boolean, write: Boolean, administrate: Boolean)
