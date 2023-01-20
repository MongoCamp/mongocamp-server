package dev.mongocamp.server.route

import dev.mongocamp.server.Server
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.file.FileAdapterHolder
import dev.mongocamp.server.model.MongoCampConfigurationExtensions._
import dev.mongocamp.server.model.auth.UserInformation
import dev.mongocamp.server.model.{ JsonValue, MongoCampConfiguration, SettingsResponse }
import dev.mongocamp.server.monitoring.{ Metric, MetricsConfiguration }
import dev.mongocamp.server.plugin.RoutesPlugin
import dev.mongocamp.server.service.ConfigurationService
import io.circe.generic.auto._
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

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
    .serverLogic(_ => _ => systemMetrics())

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
    .serverLogic(_ => _ => mongoDbMetrics())

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
    .serverLogic(_ => _ => eventMetrics())

  def eventMetrics(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[Metric]]] = {
    Future.successful(Right({
      val meters = MetricsConfiguration.getEventMetricsRegistries.head.getMeters.asScala.toList
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
      val configurations: Map[String, Any] =
        ListMap(ConfigurationService.getAllRegisteredConfigurations().map(config => config.key -> config.typedValue()).toMap.toSeq.sortBy(_._1): _*)
      Right(
        SettingsResponse(
          Server.listOfRoutePlugins.map(_.getClass.getName),
          FileAdapterHolder.listOfFilePlugins.map(_.getClass.getName),
          ConfigurationService.getConfigValue[List[String]](DefaultConfigurations.ConfigKeyPluginsIgnored),
          configurations
        )
      )
    }
  }

  val listConfigurationEndpoint = applicationApiBaseEndpoint
    .in("system" / "configurations")
    .out(jsonBody[List[MongoCampConfiguration]])
    .summary("List Configurations")
    .description("List all Configurations or filtered")
    .method(Method.GET)
    .name("listConfigurations")
    .serverLogic(_ => _ => listConfigurations())

  def listConfigurations(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[MongoCampConfiguration]]] = {
    Future.successful {
      Right({
        ConfigurationService.getAllRegisteredConfigurations()
      })
    }
  }

  val getConfigEndpoint = applicationApiBaseEndpoint
    .in("system" / "configurations")
    .in(path[String]("configurationKey").description("configurationKey to get"))
    .out(jsonBody[Option[MongoCampConfiguration]])
    .summary("Configuration for configurationKey")
    .description("Get Configuration for key")
    .method(Method.GET)
    .name("getConfig")
    .serverLogic(_ => loginToUpdate => getConfig(loginToUpdate))

  def getConfig(configKey: String): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Option[MongoCampConfiguration]]] = {
    Future.successful {
      Right({
        ConfigurationService.getConfig(configKey)
      })
    }
  }

  val updateConfigEndpoint = applicationApiBaseEndpoint
    .in("system" / "configurations")
    .in(path[String]("configurationKey").description("configurationKey to edit"))
    .in(jsonBody[JsonValue[Any]])
    .out(jsonBody[JsonValue[Boolean]])
    .summary("Update Configuration")
    .description("Update Configuration with the value")
    .method(Method.PATCH)
    .name("updateConfiguration")
    .serverLogic(userInformation => loginToUpdate => updateConfig(userInformation, loginToUpdate))

  def updateConfig(
      userInformation: UserInformation,
      parameter: (String, JsonValue[Any])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful {
      Right({
        val response = ConfigurationService.updateConfig(parameter._1, parameter._2.value)
        JsonValue(response)
      })
    }
  }

  override def endpoints = {
    var endpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = List(
      settingsEndpoint,
      listConfigurationEndpoint,
      getConfigEndpoint,
      updateConfigEndpoint
    )
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
