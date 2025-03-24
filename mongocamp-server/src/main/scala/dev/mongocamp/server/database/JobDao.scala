package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.model.JobConfig
import io.circe.generic.auto._

case class JobDao() extends MongoDAO[JobConfig](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameJobs)
