package dev.mongocamp.server.event.file

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.UserInformation
import dev.mongocamp.server.model.UpdateResponse

case class UpdateFileEvent(userInformation: UserInformation, updateResponse: UpdateResponse) extends Event
