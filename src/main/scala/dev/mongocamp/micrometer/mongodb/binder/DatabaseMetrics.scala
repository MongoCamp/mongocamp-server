package dev.mongocamp.micrometer.mongodb.binder

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.micrometer.mongodb.MetricsCache.metricsCache
import io.micrometer.core.instrument.binder.{ BaseUnits, MeterBinder }
import io.micrometer.core.instrument.{ Gauge, MeterRegistry, Tag }
import org.mongodb.scala.{ Document, MongoDatabase }

import scala.jdk.CollectionConverters.IterableHasAsJava

case class DatabaseMetrics(mongoDatabase: MongoDatabase, tags: List[Tag] = List()) extends MeterBinder {

  override def bindTo(registry: MeterRegistry): Unit = {
    mongoDatabase.listCollectionNames().resultList().foreach(collectionName => CollectionMetrics(mongoDatabase, collectionName, tags).bindTo(registry))
  }

}
