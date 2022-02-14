package com.quadstingray.mongo.rest.model.auth

case class CollectionGrant(collection: String, read: Boolean, write: Boolean, administrate: Boolean)
