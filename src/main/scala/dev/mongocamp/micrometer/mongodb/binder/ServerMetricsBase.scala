package dev.mongocamp.micrometer.mongodb.binder

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.micrometer.mongodb.MetricsCache
import io.micrometer.core.instrument.binder.MeterBinder
import org.mongodb.scala.{Document, MongoDatabase}
trait ServerMetricsBase extends MeterBinder {

  def mongoDatabase: MongoDatabase
  protected def getServerStats: Document = {
    val cacheKey = s"ServerStats"
    val cachedDocument = MetricsCache.getMetricsCache.getIfPresent(cacheKey)
    cachedDocument.getOrElse({
      val freshDocument = refreshStatsFromDatabase
      MetricsCache.getMetricsCache.put(cacheKey, freshDocument)
      freshDocument
    })
  }

  private def refreshStatsFromDatabase: Document = {
    mongoDatabase
      .runCommand(Map("serverStatus" -> 1))
      .result()
  }
}
