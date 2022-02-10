package com.quadstingray.mongo.rest.routes.docs

import com.quadstingray.mongo.rest.BuildInfo
import com.quadstingray.mongo.rest.config.Config
import com.quadstingray.mongo.rest.exception.ErrorDescription
import com.quadstingray.mongo.rest.routes.BaseRoute
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.openapi.OpenAPI
import sttp.tapir.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.SwaggerUI

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

object ApiDocsRoutes extends BaseRoute with Config {
  val nameAsyncApiDocsYamlName = "asyncapidocs.yaml"
  val nameOpenApiDocsYamlName  = "docs.yaml"

  lazy val isSwaggerEnabled: Boolean = globalConfigBoolean("mongorest.docs.swagger")
  lazy val isOpenApiEnabled: Boolean = globalConfigBoolean("mongorest.docs.openapi")

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

    if (isSwaggerEnabled || isOpenApiEnabled) {
      val openApiDocs: OpenAPI = OpenAPIDocsInterpreter().toOpenAPI(
        serverEndpoints.map(_.endpoint),
        BuildInfo.name,
        BuildInfo.version
      )
      val openApiYml: String = openApiDocs.toYaml

      if (isSwaggerEnabled) {
        val swaggerUIRoute = SwaggerUI[Future](openApiYml, List("docs"), nameOpenApiDocsYamlName)
        docs ++= swaggerUIRoute
      }

      if (!isSwaggerEnabled) {
        docs += docsYamlEndpoint(nameOpenApiDocsYamlName, openApiYml)
      }
    }

    docs.toList
  }

}
