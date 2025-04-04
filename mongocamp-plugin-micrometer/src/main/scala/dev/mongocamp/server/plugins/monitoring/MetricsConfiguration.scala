package dev.mongocamp.server.plugins.monitoring

import com.typesafe.scalalogging.LazyLogging
import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.binder.jvm._
import io.micrometer.core.instrument.binder.system.{DiskSpaceMetrics, FileDescriptorMetrics, ProcessorMetrics, UptimeMetrics}
import io.micrometer.core.instrument.{MeterRegistry, Metrics}

import java.io.File
import scala.collection.mutable.ArrayBuffer

object MetricsConfiguration extends LazyLogging{

  private lazy val jvmMetricsRegistries: ArrayBuffer[MeterRegistry]     = ArrayBuffer()
  private lazy val systemMetricsRegistries: ArrayBuffer[MeterRegistry]  = ArrayBuffer()
  private lazy val mongoDbMetricsRegistries: ArrayBuffer[MeterRegistry] = ArrayBuffer()
  private lazy val eventMetricsRegistries: ArrayBuffer[MeterRegistry]   = ArrayBuffer()

  private lazy val jvmMeterBinder: ArrayBuffer[MeterBinder] = ArrayBuffer(
    new ClassLoaderMetrics(),
    new JvmMemoryMetrics(),
    new JvmGcMetrics(),
    new JvmHeapPressureMetrics(),
    new JvmThreadMetrics(),
    new JvmInfoMetrics(),
    new JvmCompilationMetrics()
  )
  private lazy val systemMeterBinder: ArrayBuffer[MeterBinder] = ArrayBuffer(
    new DiskSpaceMetrics(new File("/")),
    new FileDescriptorMetrics(),
    new ProcessorMetrics(),
    new UptimeMetrics()
  )

  private lazy val mongoDbMeterBinder: ArrayBuffer[MeterBinder] = ArrayBuffer()
  private lazy val eventMeterBinder: ArrayBuffer[MeterBinder]   = ArrayBuffer()

  def addJvmRegistry(registry: MeterRegistry): Unit = {
    jvmMetricsRegistries += registry
    Metrics.globalRegistry.add(registry)
  }

  def addSystemRegistry(registry: MeterRegistry): Unit = {
    systemMetricsRegistries += registry
    Metrics.globalRegistry.add(registry)
  }

  def addMongoRegistry(registry: MeterRegistry): Unit = {
    mongoDbMetricsRegistries += registry
    Metrics.globalRegistry.add(registry)
  }

  def addEventRegistry(registry: MeterRegistry): Unit = {
    eventMetricsRegistries += registry
    Metrics.globalRegistry.add(registry)
  }

  def getJvmMetricsRegistries = jvmMetricsRegistries.toList

  def getSystemMetricsRegistries = systemMetricsRegistries.toList

  def getMongoDbMetricsRegistries = mongoDbMetricsRegistries.toList

  def getEventMetricsRegistries = eventMetricsRegistries.toList

  def addJvmMeterBinder(meterBinder: MeterBinder): Unit = jvmMeterBinder += meterBinder

  def addSystemMeterBinder(meterBinder: MeterBinder): Unit = systemMeterBinder += meterBinder

  def addMongoDbBinder(meterBinder: MeterBinder): Unit = mongoDbMeterBinder += meterBinder

  def addEventMeterBinder(meterBinder: MeterBinder): Unit = eventMeterBinder += meterBinder

  def bindAll(): Unit = {
    getJvmMetricsRegistries.foreach(
      r => jvmMeterBinder.foreach(bindMetricToRegistry(r, _))
    )
    getSystemMetricsRegistries.foreach(
      r => systemMeterBinder.foreach(bindMetricToRegistry(r, _))
    )
    getMongoDbMetricsRegistries.foreach(
      r => {
          mongoDbMeterBinder.foreach(bindMetricToRegistry(r, _))
      }
    )
    getEventMetricsRegistries.foreach(
      r => eventMeterBinder.foreach(bindMetricToRegistry(r, _))
    )
  }

  private def bindMetricToRegistry(registry: MeterRegistry, meterBinder: MeterBinder): Unit = {
    try {
      meterBinder.bindTo(registry)
    } catch {
      case e: Throwable =>
        logger.error(s"Error binding meter to registry: ${e.getMessage}")
    }
  }

}
