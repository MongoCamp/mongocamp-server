package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.model.JobConfig

case class JobDao() extends MongoDAO[JobConfig](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameJobs)
