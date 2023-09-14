package dev.mongocamp.server.route.docs

import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.route.BaseRoute
import dev.mongocamp.server.service.ConfigurationService
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{Method, StatusCode}
import sttp.tapir._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.{SwaggerUI, SwaggerUIOptions}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

object ApiDocsRoutes extends BaseRoute {
  val nameAsyncApiDocsYamlName = "asyncapidocs.yaml"
  val nameOpenApiDocsYamlName  = "docs.yaml"

  def docsYamlEndpoint(yamlName: String, content: String): ServerEndpoint[AkkaStreams with WebSockets, Future] = {
    def contentToResponse(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (String, Long)]] = {
      Future.successful(Right({
        (content, content.getBytes.length)
      }))
    }

    val endpoint = baseEndpoint
      .in("docs" / yamlName)
      .out(stringBody)
      .out(header("Content-Type", "text/yaml"))
      .out(header("Content-Disposition", "inline; filename=\"%s\"".format(yamlName)))
      .out(header[Long]("Content-Length"))
      .tag("Docs")
      .method(Method.GET)
      .name(yamlName)
      .serverLogic(_ => contentToResponse())

    endpoint
  }

  def addDocsRoutes(serverEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]]): List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = {
    val docs = ArrayBuffer[ServerEndpoint[AkkaStreams with WebSockets, Future]]()

    val swaggerEnabled = isSwaggerEnabled
    if (swaggerEnabled || ConfigurationService.getConfigValue[Boolean](DefaultConfigurations.ConfigKeyOpenApi)) {
      val openApiDocs = OpenAPIDocsInterpreter().toOpenAPI(
        serverEndpoints.map(_.endpoint),
        BuildInfo.name,
        BuildInfo.version
      )

      val openApiYml: String = openApiDocs.toYaml

      if (swaggerEnabled) {
        val swaggerUIRoute = SwaggerUI[Future](openApiYml, SwaggerUIOptions(List("docs"), nameOpenApiDocsYamlName, List(), useRelativePaths = true))
        docs ++= swaggerUIRoute
      }

      if (!swaggerEnabled) {
        docs += docsYamlEndpoint(nameOpenApiDocsYamlName, openApiYml)
      }
    }

    docs.toList
  }

  def isSwaggerEnabled: Boolean = {
    ConfigurationService.getConfigValue[Boolean](DefaultConfigurations.ConfigKeyDocsSwagger)
  }
}
