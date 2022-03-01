package com.quadstingray.mongo.camp.model.auth

case class Role(name: String, isAdmin: Boolean, collectionGrants: List[Grant]) {}
