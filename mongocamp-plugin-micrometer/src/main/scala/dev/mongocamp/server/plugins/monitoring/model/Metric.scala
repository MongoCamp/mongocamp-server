package dev.mongocamp.server.plugins.monitoring.model

import com.typesafe.scalalogging.LazyLogging
import io.micrometer.core.instrument.Meter
import scala.jdk.CollectionConverters._

case class Metric(name: String, metricsType: String, description: String, baseUnit: String, measurements: List[Measurement])

object Metric extends LazyLogging {
  def apply(meter: Meter): Metric = {
    Metric(
      validStringValue(meter.getId.getName),
      validStringValue(meter.getId.getType.name()),
      validStringValue(meter.getId.getDescription),
      validStringValue(meter.getId.getBaseUnit),
      meter
        .measure()
        .asScala
        .toList
        .map(
          v => Measurement(validStringValue(v.getStatistic.name()), validDoubleValue(v.getValue))
        )
    )
  }

  private def validStringValue(value: String): String = {
    if (value == null) {
      ""
    }
    else {
      value
    }
  }

  // a primitive Double can not be null (the former null check was always false)
  private def validDoubleValue(value: Double): Double = value
}
