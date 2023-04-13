package dev.mongocamp.server.event.role

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.{Role, UserInformation}

case class CreateRoleEvent(userInformation: UserInformation, role: Role) extends Event
