package dev.mongocamp.server.monitoring

import dev.mongocamp.micrometer.mongodb.binder.CollectionMetrics
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.database.MongoDatabase.createNewDatabaseProvider
import io.micrometer.core.instrument.binder.jvm._
import io.micrometer.core.instrument.binder.mongodb.{MongoMetricsCommandListener, MongoMetricsConnectionPoolListener}
import io.micrometer.core.instrument.binder.system.{DiskSpaceMetrics, FileDescriptorMetrics, ProcessorMetrics, UptimeMetrics}
import io.micrometer.core.instrument.{MeterRegistry, Metrics}

import java.io.File
import scala.collection.mutable.ArrayBuffer

object MetricsConfiguration {

  private lazy val jvmMetricsRegistries: ArrayBuffer[MeterRegistry]     = ArrayBuffer()
  private lazy val systemMetricsRegistries: ArrayBuffer[MeterRegistry]  = ArrayBuffer()
  private lazy val mongoDbMetricsRegistries: ArrayBuffer[MeterRegistry] = ArrayBuffer()
  private lazy val eventMetricsRegistries: ArrayBuffer[MeterRegistry]   = ArrayBuffer()

  def addJvmRegistry(registry: MeterRegistry): Unit = {
    jvmMetricsRegistries += registry
    new ClassLoaderMetrics().bindTo(registry)
    new JvmMemoryMetrics().bindTo(registry)
    new JvmGcMetrics().bindTo(registry)
    new JvmHeapPressureMetrics().bindTo(registry)
    new JvmThreadMetrics().bindTo(registry)
    new JvmInfoMetrics().bindTo(registry)
    new JvmCompilationMetrics().bindTo(registry)
    Metrics.globalRegistry.add(registry)
  }

  def addSystemRegistry(registry: MeterRegistry): Unit = {
    systemMetricsRegistries += registry
    new DiskSpaceMetrics(new File("/")).bindTo(registry)
    new FileDescriptorMetrics().bindTo(registry)
    new ProcessorMetrics().bindTo(registry)
    new UptimeMetrics().bindTo(registry)
    Metrics.globalRegistry.add(registry)
  }

  def addMongoRegistry(registry: MeterRegistry): Unit = {
    mongoDbMetricsRegistries += registry
    CollectionMetrics(MongoDatabase.databaseProvider.database(), "monitoring_jvm").bindTo(registry)
    createNewDatabaseProvider()
    Metrics.globalRegistry.add(registry)
  }

  def addEventRegistry(registry: MeterRegistry): Unit = {
    eventMetricsRegistries += registry
    Metrics.globalRegistry.add(registry)
  }

  def getJvmMetricsRegistries     = jvmMetricsRegistries.toList
  def getSystemMetricsRegistries  = systemMetricsRegistries.toList
  def getMongoDbMetricsRegistries = mongoDbMetricsRegistries.toList
  def getEventMetricsRegistries   = eventMetricsRegistries.toList

}
