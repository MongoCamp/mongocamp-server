package dev.mongocamp.server.event.document

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.InsertResponse
import dev.mongocamp.server.model.auth.UserInformation

case class CreateDocumentEvent(userInformation: UserInformation, insertResponse: InsertResponse) extends Event
