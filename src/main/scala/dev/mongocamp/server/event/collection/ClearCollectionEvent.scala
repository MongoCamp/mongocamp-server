package dev.mongocamp.server.event.collection

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.UserInformation

case class ClearCollectionEvent(userInformation: UserInformation, collectionName: String) extends Event
