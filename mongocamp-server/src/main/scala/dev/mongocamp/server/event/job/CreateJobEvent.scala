package dev.mongocamp.server.event.job

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.auth.UserInformation
import dev.mongocamp.server.model.JobConfig

case class CreateJobEvent(userInformation: UserInformation, jobConfig: JobConfig) extends Event
