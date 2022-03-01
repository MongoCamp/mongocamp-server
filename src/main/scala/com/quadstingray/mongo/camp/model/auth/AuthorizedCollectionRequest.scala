package com.quadstingray.mongo.camp.model.auth

case class AuthorizedCollectionRequest(userInformation: UserInformation, collection: String)

object AuthorizedCollectionRequest {
  val all: String = "*"
}
