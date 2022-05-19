package dev.mongocamp.server.event.user

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.UserInformation

case class UpdateApiKeyEvent(userInformation: UserInformation, changedUserId: String) extends Event
