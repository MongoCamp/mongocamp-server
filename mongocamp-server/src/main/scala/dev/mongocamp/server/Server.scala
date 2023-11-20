package dev.mongocamp.server

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.HttpHeader.ParsingResult
import org.apache.pekko.http.scaladsl.model.HttpMethods._
import org.apache.pekko.http.scaladsl.model.{HttpHeader, HttpResponse, StatusCodes}
import org.apache.pekko.http.scaladsl.server.Directives.{complete, extractRequestContext, options, reject}
import org.apache.pekko.http.scaladsl.server.{Route, RouteConcatenation}
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.server.auth.AuthHolder
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.server.{PluginLoadedEvent, ServerStartedEvent}
import dev.mongocamp.server.interceptor.cors.Cors
import dev.mongocamp.server.interceptor.cors.Cors.{KeyCorsHeaderOrigin, KeyCorsHeaderReferer}
import dev.mongocamp.server.plugin.{RoutesPlugin, ServerPlugin}
import dev.mongocamp.server.route._
import dev.mongocamp.server.route.docs.ApiDocsRoutes
import dev.mongocamp.server.service.{ConfigurationService, PluginDownloadService, PluginService, ReflectionService}
import sttp.capabilities.WebSockets
import sttp.capabilities.pekko.PekkoStreams
import sttp.tapir.server.ServerEndpoint

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}

object Server extends App with LazyLogging with RouteConcatenation with RestServer {

  implicit private lazy val actorSystem: ActorSystem = ActorHandler.requestActorSystem
  implicit private lazy val ex: ExecutionContext     = ActorHandler.requestExecutionContext

  lazy val interface: String = ConfigurationService.getConfigValue[String](DefaultConfigurations.ConfigKeyServerInterface)
  lazy val port: Int         = ConfigurationService.getConfigValue[Long](DefaultConfigurations.ConfigKeyServerPort).toInt

  private lazy val preLoadedRoutes: ArrayBuffer[Route] = ArrayBuffer()
  private lazy val afterLoadedRoutes: ArrayBuffer[Route] = ArrayBuffer()
  private lazy val afterServerStartCallBacks: ArrayBuffer[() => Unit] = ArrayBuffer()
  private var routesPluginList: List[RoutesPlugin] = List()

  private def initializeRoutesPlugin: List[RoutesPlugin] = {
    val pluginList = ReflectionService
      .instancesForType(classOf[RoutesPlugin])
      .filterNot(plugin => ConfigurationService.getConfigValue[List[String]](DefaultConfigurations.ConfigKeyPluginsIgnored).contains(plugin.getClass.getName))
      .map(plugin => {
        EventSystem.eventStream.publish(PluginLoadedEvent(plugin.getClass.getName, "RoutesPlugin"))
        plugin
      })
    routesPluginList = pluginList
    pluginList
  }


  private def serverEndpoints: List[ServerEndpoint[PekkoStreams with WebSockets, Future]] = {
    AuthRoutes.endpoints ++ initializeRoutesPlugin.flatMap(_.endpoints)
  }

  private def routes: Route = {
    val internalEndPoints = serverEndpoints ++ ApiDocsRoutes.addDocsRoutes(serverEndpoints)
    val allEndpoints      = internalEndPoints.map(ep => HttpServer.httpServerInterpreter.toRoute(ep))
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
          headers += org.apache.pekko.http.scaladsl.model.headers.`Access-Control-Allow-Methods`(Seq(OPTIONS, POST, PUT, PATCH, GET, DELETE))
          HttpResponse(StatusCodes.OK).withHeaders(headers.toList)
        })
      }
    }
  }

  private def routeHandler(r: Route): Route = {
    preflightRequestHandler ~ preLoadedRoutes.foldLeft[Route](reject)(_ ~ _) ~ r ~ afterLoadedRoutes.foldLeft[Route](reject)(_ ~ _)
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
    ConfigurationService.registerMongoCampServerDefaultConfigs()
    val pluginDownloadService = new PluginDownloadService()
    pluginDownloadService.downloadPlugins()
    PluginService.loadPlugins()
    ReflectionService.registerClassLoaders(getClass)
    doBeforeServerStartUp()
    Http()
      .newServerAt(interface, port)
      .bindFlow(routeHandler(routes))
      .map(serverBinding => {
        AuthHolder.handler

        logger.warn("init server with interface: %s at port: %s".format(interface, port))

        if (ApiDocsRoutes.isSwaggerEnabled) {
          logger.warn("For Swagger go to: http://%s:%s/docs".format(interface, port))
        }

        EventSystem.eventStream.publish(ServerStartedEvent())
        doAfterServerStartUp()
        serverBinding
      })
  }

  private def doBeforeServerStartUp(): Unit = {
    activateServerPlugins()
  }

  private def doAfterServerStartUp(): Unit = {
    afterServerStartCallBacks.foreach(f => f())
  }

  def listOfRoutePlugins: List[RoutesPlugin] = routesPluginList

  def registerAfterStartCallBack(f: () => Unit): Unit = {
    afterServerStartCallBacks.addOne(f)
  }

  def registerPreLoadedRoutes(r: Route): Unit = {
    preLoadedRoutes.addOne(r)
  }

  def registerAfterLoadedRoutes(r: Route): Unit = {
    afterLoadedRoutes.addOne(r)
  }

  startServer()

  override def registerMongoCampServerDefaultConfigs(): Unit = ConfigurationService.registerMongoCampServerDefaultConfigs()
}
