package dev.mongocamp.server.event.role

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.{UpdateRoleRequest, UserInformation}

case class UpdateRoleEvent(userInformation: UserInformation, role: String, updateRoleRequest: UpdateRoleRequest) extends Event
