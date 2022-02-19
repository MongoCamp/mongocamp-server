package com.quadstingray.mongo.camp.model.auth

case class CollectionGrant(collection: String, read: Boolean, write: Boolean, administrate: Boolean)
