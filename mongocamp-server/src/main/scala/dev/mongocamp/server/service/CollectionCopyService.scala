package dev.mongocamp.server.service

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.converter.MongoCampBsonConverter
import dev.mongocamp.server.database.MongoDatabase
import org.bson.conversions.Bson
import org.bson.BsonDocument
import org.bson.BsonString
import java.util.UUID

/** Copies a collection in the database. The copy is made in a temporary collection and renamed to the target at the end: the target (when it is replaced) is
  * only touched when the copy is complete, a failure leaves it as it was and the temporary collection is dropped.
  */
object CollectionCopyService {

  case class CopyResult(count: Long, indexes: Int)

  def copy(
    source: CollectionNameService.CollectionName,
    target: CollectionNameService.CollectionName,
    filter: Map[String, Any],
    withIndexes: Boolean,
    replaceTarget: Boolean,
    temporaryPrefix: String
  ): CopyResult = {
    val provider  = MongoDatabase.databaseProvider
    val database  = source.database.getOrElse(provider.DefaultDatabaseName)
    val temporary = CollectionNameService.CollectionName(source.database, s"${temporaryPrefix}copy_${UUID.randomUUID().toString.replace("-", "")}")
    try {
      // $out is run by the driver: the HTTP route refuses it for the clients
      val matchStage: Seq[Bson] =
        if (filter.isEmpty) Seq() else Seq(new BsonDocument("$match", mapToBson(MongoCampBsonConverter.convertFields(filter)).toBsonDocument()))
      val pipeline: Seq[Bson] = matchStage ++ Seq(new BsonDocument("$out", new BsonString(temporary.name)))
      provider.collection(source.full).aggregate(pipeline).allowDiskUse(true).toCollection().result()
      // $out creates no collection for an empty result in every version
      if (!provider.collectionNames(database).contains(temporary.name)) {
        provider.database(database).createCollection(temporary.name).result()
      }
      val indexCount = if (withIndexes) copyIndexes(source, temporary) else 0
      provider
        .collection(temporary.full)
        .renameCollection(new com.mongodb.MongoNamespace(database, target.name), new com.mongodb.client.model.RenameCollectionOptions().dropTarget(replaceTarget))
        .result()
      CopyResult(provider.dao(target.full).count().result(), indexCount)
    }
    finally {
      if (provider.collectionNames(database).contains(temporary.name)) {
        provider.dao(temporary.full).drop().result()
      }
    }
  }

  private def copyIndexes(source: CollectionNameService.CollectionName, temporary: CollectionNameService.CollectionName): Int = {
    val provider = MongoDatabase.databaseProvider
    val indices  = provider.collection(source.full).listIndexes[BsonDocument]().resultList().filterNot(_.getString("name").getValue == "_id_")
    indices.foreach(
      index => provider.collection(temporary.full).createIndex(IndexCreateService.keysFromIndexDocument(index), IndexCreateService.optionsFromIndexDocument(index)).result()
    )
    indices.size
  }
}
