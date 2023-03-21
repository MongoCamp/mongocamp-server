package dev.mongocamp.micrometer.mongodb.binder

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.micrometer.mongodb.MetricsCache.metricsCache
import io.micrometer.core.instrument._
import io.micrometer.core.instrument.binder.{BaseUnits, MeterBinder}
import org.mongodb.scala.{Document, MongoDatabase}

import scala.jdk.CollectionConverters.IterableHasAsJava

case class NetworkMetrics(mongoDatabase: MongoDatabase, tags: List[Tag] = List()) extends ServerMetricsBase {
  private val namePrefix = "mongodb.server.status.network"

  override def bindTo(registry: MeterRegistry): Unit = {
    Gauge
      .builder(s"$namePrefix.bytesIn", () => getServerStats.getDoubleValue("network.bytesIn"))
      .tags(tags.asJava)
      .description("The total number of bytes that the server has received over network")
      .baseUnit(BaseUnits.BYTES)
      .register(registry)

    Gauge
      .builder(s"$namePrefix.bytesOut", () => getServerStats.getDoubleValue("network.bytesOut"))
      .tags(tags.asJava)
      .description("The total number of bytes that the server has sent over network")
      .baseUnit(BaseUnits.BYTES)
      .register(registry)

  }

}
