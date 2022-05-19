package dev.mongocamp.server.event.document
import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.UpdateResponse
import dev.mongocamp.server.model.auth.UserInformation

case class UpdateDocumentEvent(userInformation: UserInformation, updateResponse: UpdateResponse) extends Event
