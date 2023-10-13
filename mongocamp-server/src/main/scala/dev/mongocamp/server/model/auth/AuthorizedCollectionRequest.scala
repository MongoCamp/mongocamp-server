package dev.mongocamp.server.model.auth

case class AuthorizedCollectionRequest(userInformation: UserInformation, collection: String)

object AuthorizedCollectionRequest {
  val all: String = "*"
}
