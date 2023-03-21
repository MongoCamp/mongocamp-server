package dev.mongocamp.micrometer.mongodb.binder

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.micrometer.mongodb.MetricsCache.metricsCache
import io.micrometer.core.instrument.binder.{ BaseUnits, MeterBinder }
import io.micrometer.core.instrument._
import org.mongodb.scala.{ Document, MongoDatabase }

import scala.jdk.CollectionConverters.IterableHasAsJava

case class ServerMetrics(mongoDatabase: MongoDatabase, tags: List[Tag] = List()) extends ServerMetricsBase {
  private val namePrefix = "mongodb.server.status"

  override def bindTo(registry: MeterRegistry): Unit = {

    Gauge
      .builder(s"$namePrefix.uptime", () => getServerStats.getDoubleValue("uptimeMillis"))
      .tags(tags.asJava)
      .description("The uptime of your Server in Seconds")
      .baseUnit(BaseUnits.MILLISECONDS)
      .register(registry)

  }

}
