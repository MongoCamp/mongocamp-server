package dev.mongocamp.server.event.file

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.InsertResponse
import dev.mongocamp.server.model.auth.UserInformation

case class CreateFileEvent(userInformation: UserInformation, insertResponse: InsertResponse) extends Event
