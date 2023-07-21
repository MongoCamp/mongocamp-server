package dev.mongocamp.server.event.job

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.UserInformation

case class DeleteJobEvent(userInformation: UserInformation, jobName: String, jobGroup: String) extends Event
