package dev.mongocamp.server.monitoring

import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.binder.jvm._
import io.micrometer.core.instrument.binder.mongodb.{ MongoMetricsCommandListener, MongoMetricsConnectionPoolListener }
import io.micrometer.core.instrument.binder.system.{ DiskSpaceMetrics, FileDescriptorMetrics, ProcessorMetrics, UptimeMetrics }
import io.micrometer.core.instrument.simple.SimpleMeterRegistry

import java.io.File

object MetricsConfiguration {

  val jvmRegistry     = new SimpleMeterRegistry()
  val systemRegistry  = new SimpleMeterRegistry()
  val mongoDbRegistry = new SimpleMeterRegistry()
  val eventRegistry   = new SimpleMeterRegistry()

  Metrics.globalRegistry.add(jvmRegistry)
  Metrics.globalRegistry.add(systemRegistry)
  Metrics.globalRegistry.add(mongoDbRegistry)
  Metrics.globalRegistry.add(eventRegistry)

  registerJvmMonitoring()
  registerSystemMonitoring()
  registerMongoDbMonitoring()

  private def registerJvmMonitoring() = {
    new ClassLoaderMetrics().bindTo(jvmRegistry)
    new JvmMemoryMetrics().bindTo(jvmRegistry)
    new JvmGcMetrics().bindTo(jvmRegistry)
    new JvmHeapPressureMetrics().bindTo(jvmRegistry)
    new JvmThreadMetrics().bindTo(jvmRegistry)
  }

  private def registerSystemMonitoring() = {
    new DiskSpaceMetrics(new File("/")).bindTo(systemRegistry)
    new FileDescriptorMetrics().bindTo(systemRegistry)
    new ProcessorMetrics().bindTo(systemRegistry)
    new UptimeMetrics().bindTo(systemRegistry)
  }

  private def registerMongoDbMonitoring() = {
    new MongoMetricsConnectionPoolListener(mongoDbRegistry)
    new MongoMetricsCommandListener(mongoDbRegistry)
  }

}
