package dev.mongocamp.micrometer.mongodb.registry

import dev.mongocamp.driver.mongodb._
import MongoStepMeterRegistry.threadFactory
import io.micrometer.common.util.StringUtils
import io.micrometer.core.instrument._
import io.micrometer.core.instrument.step.StepMeterRegistry
import io.micrometer.core.instrument.util.NamedThreadFactory
import org.mongodb.scala.Document

import java.util.Date
import java.util.concurrent.TimeUnit
import scala.collection.mutable
import scala.jdk.CollectionConverters._

class MongoStepMeterRegistry(config: MonitoringMongoConfig, threadFactory: NamedThreadFactory = threadFactory, clock: Clock = Clock.SYSTEM)
    extends StepMeterRegistry(config, clock) {

  start(threadFactory)
  override def publish(): Unit = {
    val metrics: Map[String, Any] = getMeters.asScala
      .map(meter => getConventionName(meter.getId) -> convertMeterToMap(meter))
      .toMap
      .filter(_._2.nonEmpty) ++ Map("date" -> new Date())
    val result = config.mongoDAO.insertOne(documentFromScalaMap(metrics)).result()
    result
  }

  override def getBaseTimeUnit: TimeUnit = TimeUnit.SECONDS

  private def convertMeterToMap(meter: Meter): Map[String, Any] = {
    val KeyMetricType  = "metricType"
    val KeyTags        = "tags"
    val KeySum         = "sum"
    val KeyValue       = "value"
    val KeyCount       = "count"
    val KeyMean        = "mean"
    val KeyUpper       = "upper"
    val KeyActiveTasks = "active_tasks"
    val keyDuration    = "duration"
    val id             = meter.getId
    val defaultFields =
      Map(KeyMetricType -> id.getType.name().toLowerCase(), KeyTags -> getConventionTags(id).asScala.filter(t => StringUtils.isNotBlank(t.getValue)))
    meter match {
      case g: Gauge =>
        if (java.lang.Double.isFinite(g.value())) {
          defaultFields ++ Map(KeyValue -> g.value())
        }
        else {
          Map()
        }
      case c: Counter =>
        if (java.lang.Double.isFinite(c.count())) {
          defaultFields ++ Map(KeyCount -> c.count())
        }
        else {
          Map()
        }
      case t: LongTaskTimer =>
        defaultFields ++ Map(KeyActiveTasks -> t.activeTasks(), keyDuration -> t.duration(getBaseTimeUnit))
      case t: FunctionTimer =>
        val sum = t.totalTime(getBaseTimeUnit)
        if (java.lang.Double.isFinite(sum)) {
          val fields: Map[String, Any] = defaultFields ++ Map(KeySum -> sum, KeyCount -> t.count())
          val mean                     = t.mean(getBaseTimeUnit)
          if (java.lang.Double.isFinite(mean)) {
            fields ++ Map(KeyMean -> mean)
          }
          else {
            fields
          }
        }
        else {
          Map()
        }
      case t: Timer =>
        defaultFields ++ Map(
          KeySum   -> t.totalTime(getBaseTimeUnit),
          KeyCount -> t.count(),
          KeyMean  -> t.mean(getBaseTimeUnit),
          KeyUpper -> t.max(getBaseTimeUnit)
        )
      case s: DistributionSummary =>
        defaultFields ++ Map(KeySum -> s.totalAmount(), KeyCount -> s.count(), KeyMean -> s.mean(), KeyUpper -> s.max())
      case m: Meter =>
        val fields = mutable.Map[String, Any]()
        m.measure()
          .asScala
          .foreach(measure => {
            if (java.lang.Double.isFinite(measure.getValue)) {
              val key = measure.getStatistic.getTagValueRepresentation.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase
              fields.update(key, measure.getValue)
            }
          })
        defaultFields
      case _ => Map()
    }
  }
}

object MongoStepMeterRegistry {

  private lazy val threadFactory = new NamedThreadFactory("mongodb-metrics-publisher")

  def apply(mongoDAO: MongoDAO[Document], configurationMap: Map[String, String] = Map()): MongoStepMeterRegistry = {
    new MongoStepMeterRegistry(MonitoringMongoConfig(mongoDAO, configurationMap))
  }
}
