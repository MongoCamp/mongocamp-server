package dev.mongocamp.server.event.document

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.UserInformation
import dev.mongocamp.server.model.DeleteResponse

case class DeleteDocumentEvent(userInformation: UserInformation, deleteResponse: DeleteResponse, oldValues: List[Map[String, Any]]) extends Event
