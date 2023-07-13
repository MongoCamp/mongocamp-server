package dev.mongocamp.server.model

case class JobConfig(
    name: String,
    className: String,
    description: String,
    cronExpression: String,
    group: String = ModelConstants.jobDefaultGroup,
    priority: Int = ModelConstants.jobDefaultPriority
)
