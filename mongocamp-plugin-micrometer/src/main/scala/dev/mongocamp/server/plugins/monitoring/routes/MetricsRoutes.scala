package dev.mongocamp.server.plugins.monitoring.routes

import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.plugin.RoutesPlugin
import dev.mongocamp.server.plugins.monitoring.MetricsConfiguration
import dev.mongocamp.server.plugins.monitoring.model.Metric
import dev.mongocamp.server.route.BaseRoute
import io.circe.generic.auto._
import sttp.capabilities.WebSockets
import sttp.capabilities.pekko.PekkoStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future
import scala.jdk.CollectionConverters._

object MetricsRoutes extends BaseRoute with RoutesPlugin {
  private val applicationApiBaseEndpoint = adminEndpoint.tag("Application")

  val jvmMetricsRoutes = applicationApiBaseEndpoint
    .in("system" / "monitoring" / "jvm")
    .out(jsonBody[List[Metric]])
    .summary("JVM Metrics")
    .description("Returns the JVM Metrics of the running MongoCamp Application")
    .method(Method.GET)
    .name("jvmMetrics")
    .serverLogic(
      _ => _ => jvmMetrics()
    )

  def jvmMetrics(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Metric]]] = {
    Future.successful(Right({
      val meters = MetricsConfiguration.getJvmMetricsRegistries.head.getMeters.asScala.toList
      meters.map(Metric(_))
    }))
  }

  val systemMetricsRoutes = applicationApiBaseEndpoint
    .in("system" / "monitoring" / "system")
    .out(jsonBody[List[Metric]])
    .summary("System Metrics")
    .description("Returns the Metrics of the MongoCamp System")
    .method(Method.GET)
    .name("systemMetrics")
    .serverLogic(
      _ => _ => systemMetrics()
    )

  def systemMetrics(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Metric]]] = {
    Future.successful(Right({
      val meters = MetricsConfiguration.getSystemMetricsRegistries.head.getMeters.asScala.toList
      meters.map(Metric(_))
    }))
  }

  val mongoDbMetricsRoutes = applicationApiBaseEndpoint
    .in("system" / "monitoring" / "mongodb")
    .out(jsonBody[List[Metric]])
    .summary("MongoDb Metrics")
    .description("Returns the MongoDB Metrics of the running MongoCamp Application")
    .method(Method.GET)
    .name("mongoDbMetrics")
    .serverLogic(
      _ => _ => mongoDbMetrics()
    )

  def mongoDbMetrics(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Metric]]] = {
    Future.successful(Right({
      val meters = MetricsConfiguration.getMongoDbMetricsRegistries.head.getMeters.asScala.toList
      meters.map(Metric(_))
    }))
  }

  val eventMetricsRoutes = applicationApiBaseEndpoint
    .in("system" / "monitoring" / "events")
    .out(jsonBody[List[Metric]])
    .summary("Event Metrics")
    .description("Returns the Metrics of events of the running MongoCamp Application.")
    .method(Method.GET)
    .name("eventMetrics")
    .serverLogic(
      _ => _ => eventMetrics()
    )

  def eventMetrics(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Metric]]] = {
    Future.successful(Right({
      val meters = MetricsConfiguration.getEventMetricsRegistries.head.getMeters.asScala.toList
      meters.map(Metric(_))
    }))
  }

  override def endpoints = {
    var endpoints: List[ServerEndpoint[PekkoStreams with WebSockets, Future]] = List()
    if (MetricsConfiguration.getJvmMetricsRegistries.nonEmpty) {
      endpoints ++= List(jvmMetricsRoutes)
    }
    if (MetricsConfiguration.getSystemMetricsRegistries.nonEmpty) {
      endpoints ++= List(systemMetricsRoutes)
    }
    if (MetricsConfiguration.getMongoDbMetricsRegistries.nonEmpty) {
      endpoints ++= List(mongoDbMetricsRoutes)
    }
    if (MetricsConfiguration.getEventMetricsRegistries.nonEmpty) {
      endpoints ++= List(eventMetricsRoutes)
    }
    endpoints
  }

}
