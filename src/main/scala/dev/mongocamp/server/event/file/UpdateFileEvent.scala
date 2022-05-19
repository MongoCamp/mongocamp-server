package dev.mongocamp.server.event.file

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.UpdateResponse
import dev.mongocamp.server.model.auth.UserInformation

case class UpdateFileEvent(userInformation: UserInformation, updateResponse: UpdateResponse) extends Event
