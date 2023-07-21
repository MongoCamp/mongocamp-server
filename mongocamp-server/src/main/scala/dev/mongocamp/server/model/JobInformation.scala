package dev.mongocamp.server.model

import java.util.Date

case class JobInformation(
    name: String,
    group: String,
    jobClassName: String,
    description: String,
    cronExpression: String,
    priority: Int,
    lastScheduledFireTime: Option[Date],
    nextScheduledFireTime: Option[Date],
    scheduleInformation: Option[String]
)
