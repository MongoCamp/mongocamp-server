package dev.mongocamp.server.event.bucket

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.UserInformation

case class ClearBucketEvent(userInformation: UserInformation, bucketName: String) extends Event
