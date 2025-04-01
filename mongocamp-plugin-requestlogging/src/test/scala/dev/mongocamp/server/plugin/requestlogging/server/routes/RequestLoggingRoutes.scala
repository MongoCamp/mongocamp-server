package dev.mongocamp.server.plugin.requestlogging.server.routes

import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.model.JsonValue
import dev.mongocamp.server.plugin.RoutesPlugin
import dev.mongocamp.server.route.BaseRoute
import io.circe.generic.auto._
import scala.concurrent.duration.DurationInt
import scala.concurrent.Future
import sttp.capabilities.pekko.PekkoStreams
import sttp.capabilities.WebSockets
import sttp.model.Method
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

object RequestLoggingRoutes extends BaseRoute with RoutesPlugin {

  val blockingTestRoute = baseEndpoint
    .tag("TestEndpoint")
    .in("blocking")
    .out(jsonBody[JsonValue[Boolean]])
    .summary("Test Request")
    .description("Test Request with 5s blocking")
    .method(Method.GET)
    .name("blocking")
    .serverLogic(
      _ => blockingMethod()
    )

  def blockingMethod(): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful(Right {
      Thread.sleep(5.seconds.toMillis)
      JsonValue(true)
    })
  }

  override def endpoints: List[ServerEndpoint[PekkoStreams with WebSockets, Future]] = {
    List(blockingTestRoute)
  }
}
