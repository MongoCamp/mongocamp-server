package dev.mongocamp.server.event.user

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.UserInformation

case class DeleteUserEvent(userInformation: UserInformation, deletedUser: String) extends Event
