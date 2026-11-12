package dev.mongocamp.server.event.collection

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.UserInformation

/** The validation options of a collection were changed. */
case class ModifyCollectionEvent(userInformation: UserInformation, collectionName: String) extends Event
