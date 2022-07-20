package dev.mongocamp.server.model

case class JobConfig(
                            name: String,
                            className: String,
                            description: String,
                            cronExpression: String,
                            group: String  = JobConfig.defaultGroup,
                            priority: Int = 1
                          )

object JobConfig {
  val defaultGroup: String = "Default"
}