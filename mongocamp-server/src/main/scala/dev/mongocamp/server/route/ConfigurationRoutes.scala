package dev.mongocamp.server.route

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.Server
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.file.FileAdapterHolder
import dev.mongocamp.server.library.BuildInfo
import dev.mongocamp.server.model.MongoCampConfigurationExtensions._
import dev.mongocamp.server.model.auth.UserInformation
import dev.mongocamp.server.model.{ JsonValue, MongoCampConfiguration, SettingsResponse }
import dev.mongocamp.server.plugin.RoutesPlugin
import dev.mongocamp.server.service.ConfigurationService
import io.circe.generic.auto._
import sttp.capabilities.WebSockets
import sttp.capabilities.pekko.PekkoStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.collection.immutable.ListMap
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object ConfigurationRoutes extends BaseRoute with RoutesPlugin with LazyLogging {
  private val applicationApiBaseEndpoint = adminEndpoint.tag("Application")

  val settingsEndpoint = applicationApiBaseEndpoint
    .in("system" / "settings")
    .out(jsonBody[SettingsResponse])
    .summary("System Settings")
    .description("Returns the Settings of the running MongoCamp Application.")
    .method(Method.GET)
    .name("settings")
    .serverLogic(
      _ => _ => systemSettings()
    )

  def systemSettings(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), SettingsResponse]] = {
    Future.successful {
      val configurations: Map[String, Any] =
        ListMap(
          ConfigurationService
            .getAllRegisteredConfigurations()
            .map(
              config => config.key -> config.typedValue()
            )
            .toMap
            .toSeq
            .sortBy(_._1): _*
        )
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
    .serverLogic(
      _ => _ => listConfigurations()
    )

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
    .serverLogic(
      _ => loginToUpdate => getConfig(loginToUpdate)
    )

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
    .serverLogic(
      userInformation => loginToUpdate => updateConfig(userInformation, loginToUpdate)
    )

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

  val shutdownEndpoint = applicationApiBaseEndpoint
    .in("system")
    .in(query[Boolean]("force").description("Shutdown Server and don`t send reboot event.").default(false))
    .out(jsonBody[JsonValue[Boolean]])
    .summary("Shutdown MongoCamp")
    .description("Shutdown the running MongoCamp Application. CLI Mode will automatically restart the Application.")
    .method(Method.DELETE)
    .name("shutdown")
    .serverLogic(
      _ => force => shutdown(force)
    )

  def shutdown(force: Boolean): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful {
      Right {
        Future {
          if (!force) {
            val temp              = File(File.temp.pathAsString + s"/mongocamp_${BuildInfo.version}")
            val tmpFile           = File.newTemporaryFile(parent = Some(temp))
            val pid               = ProcessHandle.current.pid
            val shutdownTimestamp = System.currentTimeMillis() + 1.seconds.toMillis
            tmpFile.write("{\"event\":\"shutdown\",\"timestamp\":\"" + shutdownTimestamp + "\",\"pid\":\"" + pid + "\"}")
            tmpFile.appendLine()
            logger.trace(s"Shutdown triggered. File written. $tmpFile.")
            while (java.lang.System.currentTimeMillis() < shutdownTimestamp) {}
          }
          java.lang.System.exit(0)
        }(scala.concurrent.ExecutionContext.global)
        JsonValue(true)
      }
    }
  }

  override def endpoints = {
    var endpoints: List[ServerEndpoint[PekkoStreams with WebSockets, Future]] = List(
      settingsEndpoint,
      listConfigurationEndpoint,
      getConfigEndpoint,
      updateConfigEndpoint,
      shutdownEndpoint
    )
    endpoints
  }

}
