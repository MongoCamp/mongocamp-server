package dev.mongocamp.server.event.document
import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.DeleteResponse
import dev.mongocamp.server.model.auth.UserInformation

case class DeleteDocumentEvent(userInformation: UserInformation, deleteResponse: DeleteResponse) extends Event
