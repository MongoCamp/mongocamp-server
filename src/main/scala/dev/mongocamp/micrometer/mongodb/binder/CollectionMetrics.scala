package dev.mongocamp.micrometer.mongodb.binder

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.micrometer.mongodb.MetricsCache.metricsCache
import io.micrometer.core.instrument.binder.{BaseUnits, MeterBinder}
import io.micrometer.core.instrument.{Gauge, MeterRegistry, Tag}
import org.mongodb.scala.{Document, MongoDatabase}

import scala.jdk.CollectionConverters.IterableHasAsJava

case class CollectionMetrics(mongoDatabase: MongoDatabase, collectionName: String, tags: List[Tag] = List()) extends MeterBinder {
  private val namePrefix = s"mongodb.collection.${mongoDatabase.name}.$collectionName"

  override def bindTo(registry: MeterRegistry): Unit = {
    Gauge
      .builder(s"$namePrefix.size", () => getCollectionStats.getDoubleValue("size"))
      .tags(tags.asJava)
      .description(s"The total size of all documents for the collection ${collectionName}.")
      .baseUnit(BaseUnits.BYTES)
      .register(registry)

    Gauge
      .builder(s"$namePrefix.totalIndexSize", () => getCollectionStats.getDoubleValue("totalIndexSize"))
      .tags(tags.asJava)
      .description(s"The total size of all indexes for the collection ${collectionName}.")
      .baseUnit(BaseUnits.BYTES)
      .register(registry)

    Gauge
      .builder(s"$namePrefix.avgObjSize", () => getCollectionStats.getDoubleValue("avgObjSize"))
      .tags(tags.asJava)
      .description(s"The avg size of documents for the collection ${collectionName}.")
      .baseUnit(BaseUnits.BYTES)
      .register(registry)

    Gauge
      .builder(s"$namePrefix.count", () => getCollectionStats.getDoubleValue("count"))
      .tags(tags.asJava)
      .description(s"The total number of documents in the collection ${collectionName} to the return document.")
      .baseUnit(BaseUnits.BYTES)
      .register(registry)
  }

  private def getCollectionStats: Document = {
    val cacheKey = s"${mongoDatabase.name}:::${collectionName}"
    val cachedDocument = metricsCache.getIfPresent(cacheKey)
    cachedDocument.getOrElse({
      val freshDocument = refreshCollectionStatsFromDatabase
      metricsCache.put(cacheKey, freshDocument)
      freshDocument
    })
  }

  private def refreshCollectionStatsFromDatabase: Document = {
    mongoDatabase
      .runCommand(Map("collStats" -> collectionName))
      .result()
  }

}
