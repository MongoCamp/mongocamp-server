package dev.mongocamp.micrometer.mongodb.binder

import io.micrometer.core.instrument.binder.MeterBinder
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.micrometer.mongodb.MetricsCache.metricsCache
import io.micrometer.core.instrument.binder.{ BaseUnits, MeterBinder }
import io.micrometer.core.instrument._
import org.mongodb.scala.{ Document, MongoDatabase }

import scala.jdk.CollectionConverters.IterableHasAsJava
trait ServerMetricsBase extends MeterBinder {

  def mongoDatabase: MongoDatabase
  protected def getServerStats: Document = {
    val cacheKey = s"ServerStats"
    val cachedDocument = metricsCache.getIfPresent(cacheKey)
    cachedDocument.getOrElse({
      val freshDocument = refreshStatsFromDatabase
      metricsCache.put(cacheKey, freshDocument)
      freshDocument
    })
  }

  private def refreshStatsFromDatabase: Document = {
    mongoDatabase
      .runCommand(Map("serverStatus" -> 1))
      .result()
  }
}
