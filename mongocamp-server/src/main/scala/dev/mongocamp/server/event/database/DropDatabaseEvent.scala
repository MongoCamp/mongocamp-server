package dev.mongocamp.server.event.database

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.UserInformation

case class DropDatabaseEvent(userInformation: UserInformation, databaseName: String) extends Event
