package dev.mongocamp.server.service

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.driver.mongodb.database.CollectionStatus
import dev.mongocamp.driver.mongodb.database.DatabaseProvider.CollectionSeparator
import dev.mongocamp.server.database.MongoDatabase

/** `collStats` of a collection, `name` in the default database or `database:name` in another. The status of the DAO of the driver does not know the separator
  * and answers the numbers of a collection that does not exist (all 0), this one is used for collections and for the collections of buckets.
  */
object CollectionStatusService {

  def statusOf(collectionName: String): CollectionStatus = {
    val mongoDatabase = if (collectionName.contains(CollectionSeparator)) {
      MongoDatabase.databaseProvider.database(collectionName.split(CollectionSeparator).head)
    }
    else {
      MongoDatabase.databaseProvider.database()
    }
    val collection = collectionName.split(CollectionSeparator).last
    mongoDatabase
      .runCommand(Map("collStats" -> collection))
      .map(
        document => CollectionStatus(document)
      )
      .result()
  }
}
