package dev.mongocamp.server.client.model

import org.joda.time.DateTime

case class JobInformation(
    name: String,
    group: String,
    jobClassName: String,
    description: String,
    cronExpression: String,
    priority: Int,
    lastScheduledFireTime: Option[DateTime] = None,
    nextScheduledFireTime: Option[DateTime] = None,
    scheduleInformation: Option[String] = None
)
