package dev.mongocamp.server.event.collection

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.UserInformation

case class RenameCollectionEvent(userInformation: UserInformation, collectionName: String, newCollectionName: String) extends Event
