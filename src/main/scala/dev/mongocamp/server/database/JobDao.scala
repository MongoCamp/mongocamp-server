package dev.mongocamp.server.database

import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.model.JobConfigDetail

case class JobDao() extends MongoDAO[JobConfigDetail](MongoDatabase.databaseProvider, MongoDatabase.CollectionNameJobs)
