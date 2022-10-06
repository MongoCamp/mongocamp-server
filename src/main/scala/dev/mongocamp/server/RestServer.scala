package dev.mongocamp.server

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.`Access-Control-Allow-Methods`
import akka.http.scaladsl.model.{HttpHeader, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, extractRequestContext, options}
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.auth.AuthHolder
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.event.http.HttpRequestEvent
import dev.mongocamp.server.event.listener.{MetricsLoggingActor, RequestLoggingActor}
import dev.mongocamp.server.event.server.{PluginLoadedEvent, ServerStartedEvent}
import dev.mongocamp.server.event.{Event, EventSystem}
import dev.mongocamp.server.interceptor.cors.Cors
import dev.mongocamp.server.interceptor.cors.Cors.{KeyCorsHeaderOrigin, KeyCorsHeaderReferer}
import dev.mongocamp.server.plugin.ServerPlugin
import dev.mongocamp.server.route.docs.ApiDocsRoutes
import dev.mongocamp.server.service.{ConfigurationService, PluginService, ReflectionService}
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.server.ServerEndpoint

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}

trait RestServer extends LazyLogging with RouteConcatenation {

  implicit val actorSystem: ActorSystem = ActorHandler.requestActorSystem

  // init server parameter
  lazy val interface: String = ConfigurationService.getConfigValue[String](DefaultConfigurations.ConfigKeyServerInterface)
  lazy val port: Int         = ConfigurationService.getConfigValue[Long](DefaultConfigurations.ConfigKeyServerPort).toInt

  val serverEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]]

  def routes(implicit ex: ExecutionContext): Route = {
    val internalEndPoints = serverEndpoints ++ ApiDocsRoutes.addDocsRoutes(serverEndpoints)
    val allEndpoints      = internalEndPoints.map(ep => AkkaHttpServer.akkaHttpServerInterpreter.toRoute(ep))
    concat(allEndpoints: _*)
  }

  private def preflightRequestHandler: Route = {
    extractRequestContext { ctx =>
      options {
        complete({
          val requestHeaders = ctx.request.headers
          val originHeader   = requestHeaders.find(_.is(KeyCorsHeaderOrigin.toLowerCase())).map(_.value())
          val refererHeader = requestHeaders
            .find(_.is(KeyCorsHeaderReferer.toLowerCase()))
            .map(_.value())
            .map(string => if (string.endsWith("/")) string.replaceAll("/$", "") else string)

          val corsHeaders                      = Cors.corsHeadersFromOrigin((originHeader ++ refererHeader).headOption)
          val headers: ArrayBuffer[HttpHeader] = ArrayBuffer()
          corsHeaders.foreach(header => headers += HttpHeader.parse(header.name, header.value).asInstanceOf[ParsingResult.Ok].header)
          headers += `Access-Control-Allow-Methods`(Seq(OPTIONS, POST, PUT, PATCH, GET, DELETE))
          HttpResponse(StatusCodes.OK).withHeaders(headers.toList)
        })
      }
    }
  }

  def routeHandler(r: Route): Route = {
    preflightRequestHandler ~ r
  }

  private def activateServerPlugins(): Unit = {
    ReflectionService
      .instancesForType(classOf[ServerPlugin])
      .filterNot(plugin => ConfigurationService.getConfigValue[List[String]](DefaultConfigurations.ConfigKeyPluginsIgnored).contains(plugin.getClass.getName))
      .map(plugin => {
        plugin.activate()
        EventSystem.eventStream.publish(PluginLoadedEvent(plugin.getClass.getName, "ServerPlugin"))
        plugin
      })
  }

  def startServer()(implicit ex: ExecutionContext): Future[Unit] = {
    DefaultConfigurations.registerMongoCampServerDefaultConfigs()
    PluginService.downloadPlugins()
    PluginService.loadPlugins()
    ReflectionService.registerClassLoaders(getClass)
    doBeforeServerStartUp()
    Http()
      .newServerAt(interface, port)
      .bindFlow(routeHandler(routes))
      .map(serverBinding => {
        logger.warn("init server with interface: %s at port: %s".format(interface, port))

        if (ApiDocsRoutes.isSwaggerEnabled) {
          println("For Swagger go to: http://%s:%s/docs".format(interface, port))
        }

        AuthHolder.handler

        if (ConfigurationService.getConfigValue(DefaultConfigurations.ConfigKeyRequestLogging)) {
          val requestLoggingActor = EventSystem.eventBusActorSystem.actorOf(Props(classOf[RequestLoggingActor]), "requestLoggingActor")
          EventSystem.eventStream.subscribe(requestLoggingActor, classOf[HttpRequestEvent])
        }

        val metricsLoggingActor = EventSystem.eventBusActorSystem.actorOf(Props(classOf[MetricsLoggingActor]), "metricsLoggingActor")
        EventSystem.eventStream.subscribe(metricsLoggingActor, classOf[Event])
        EventSystem.eventStream.publish(ServerStartedEvent())
        doAfterServerStartUp()
        serverBinding
      })
  }

  def doBeforeServerStartUp(): Unit = {
    activateServerPlugins()
  }

  def doAfterServerStartUp(): Unit = {}
}
