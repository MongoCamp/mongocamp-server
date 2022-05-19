package dev.mongocamp.server.event.file

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.DeleteResponse
import dev.mongocamp.server.model.auth.UserInformation

case class DeleteFileEvent(userInformation: UserInformation, deleteResponse: DeleteResponse) extends Event
