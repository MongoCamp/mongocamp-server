package dev.mongocamp.server.route

import dev.mongocamp.server.Server
import dev.mongocamp.server.config.ConfigHolder
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.file.FileAdapterHolder
import dev.mongocamp.server.model.SettingsResponse
import dev.mongocamp.server.monitoring.{ Metric, MetricsConfiguration }
import dev.mongocamp.server.plugin.RoutesPlugin
import io.circe.generic.auto._
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

import scala.collection.immutable.ListMap
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

object ApplicationStatusRoutes extends BaseRoute with RoutesPlugin {
  private val applicationApiBaseEndpoint = adminEndpoint.tag("Application")

  val jvmMetricsRoutes = applicationApiBaseEndpoint
    .in("system" / "monitoring" / "jvm")
    .out(jsonBody[List[Metric]])
    .summary("JVM Metrics")
    .description("Returns the JVM Metrics of the running MongoCamp Application")
    .method(Method.GET)
    .name("jvmMetrics")
    .serverLogic(_ => _ => jvmMetrics())

  def jvmMetrics(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Metric]]] = {
    Future.successful(Right({
      val meters = MetricsConfiguration.jvmRegistry.getMeters.asScala.toList
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
    .serverLogic(_ => _ => systemMetrics())

  def systemMetrics(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Metric]]] = {
    Future.successful(Right({
      val meters = MetricsConfiguration.systemRegistry.getMeters.asScala.toList
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
    .serverLogic(_ => _ => mongoDbMetrics())

  def mongoDbMetrics(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Metric]]] = {
    Future.successful(Right({
      val meters = MetricsConfiguration.mongoDbRegistry.getMeters.asScala.toList
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
    .serverLogic(_ => _ => eventMetrics())

  def eventMetrics(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Metric]]] = {
    Future.successful(Right({
      val meters = MetricsConfiguration.eventRegistry.getMeters.asScala.toList
      meters.map(Metric(_))
    }))
  }

  val settingsEndpoint = applicationApiBaseEndpoint
    .in("system" / "settings")
    .out(jsonBody[SettingsResponse])
    .summary("System Settings")
    .description("Returns the Settings of the running MongoCamp Application.")
    .method(Method.GET)
    .name("settings")
    .serverLogic(_ => _ => systemSettings())

  def systemSettings(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), SettingsResponse]] = {
    Future.successful {
      val configurations = ListMap(ConfigHolder.allConfigurations.map(config => config.key -> config.value).toMap.toSeq.sortBy(_._1): _*)
      Right(
        SettingsResponse(
          Server.listOfRoutePlugins.map(_.getClass.getName),
          FileAdapterHolder.listOfFilePlugins.map(_.getClass.getName),
          ConfigHolder.pluginsIgnored.value,
          configurations
        )
      )
    }
  }

  override def endpoints = List(jvmMetricsRoutes, systemMetricsRoutes, mongoDbMetricsRoutes, eventMetricsRoutes, settingsEndpoint)
}
