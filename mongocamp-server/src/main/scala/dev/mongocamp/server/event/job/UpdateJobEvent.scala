package dev.mongocamp.server.event.job

import dev.mongocamp.server.event.Event
import dev.mongocamp.server.model.JobConfig
import dev.mongocamp.server.model.auth.UserInformation

case class UpdateJobEvent(userInformation: UserInformation, jobName: String, jobGroup: String, jobConfig: JobConfig) extends Event
