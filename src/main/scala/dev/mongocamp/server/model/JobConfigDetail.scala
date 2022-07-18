package dev.mongocamp.server.model

case class JobConfigDetail(
                            name: String,
                            group: String,
                            className: String,
                            description: String,
                            var cronExpression: String,
                            var priority: Int = 1
                          )

