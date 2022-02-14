package com.quadstingray.mongo.rest.model.auth

case class AuthorizedCollectionRequest(userInformation: UserInformation, collection: String)

object AuthorizedCollectionRequest {
  val allCollections: String = "*"
}
