package dev.mongocamp.server.event.role

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.UserInformation

case class DeleteRoleEvent(userInformation: UserInformation, deletedRole: String) extends Event
