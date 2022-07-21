package dev.mongocamp.server.client.model

case class JobConfig(
    name: String,
    className: String,
    description: String,
    cronExpression: String,
    group: String,
    priority: Int
)
