package com.quadstingray.mongo.camp.routes

import com.quadstingray.mongo.camp.database.paging.PaginationInfo
import com.quadstingray.mongo.camp.exception.ErrorDescription
import com.quadstingray.mongo.camp.model.MongoFindRequest
import com.quadstingray.mongo.camp.model.auth.AuthorizedCollectionRequest
import com.quadstingray.mongo.camp.routes.CreateRoutes._
import com.quadstingray.mongo.camp.routes.DeleteRoutes.deleteManyEndpoint
import com.quadstingray.mongo.camp.routes.ReadRoutes.findInCollection
import com.quadstingray.mongo.camp.routes.UpdateRoutes.updateManyEndpoint
import com.quadstingray.mongo.camp.routes.parameter.paging.{ Paging, PagingFunctions }
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

object DocumentRoutes extends RoutesPlugin {

  val findAllEndpoint = readCollectionEndpoint
    .in("documents")
    .in(PagingFunctions.pagingParameter)
    .out(jsonBody[List[Map[String, Any]]])
    .out(PagingFunctions.pagingHeaderOutput)
    .summary("Documents in Collection")
    .description("Get Documents paginated from MongoDatabase Collection")
    .tag("Documents")
    .method(Method.GET)
    .name("documentsList")
    .serverLogic(collectionRequest => parameter => findAllInCollection(collectionRequest, parameter))

  def findAllInCollection(
      authorizedCollectionRequest: AuthorizedCollectionRequest,
      parameter: (Paging)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (List[Map[String, Any]], PaginationInfo)]] = {
    findInCollection(authorizedCollectionRequest, (MongoFindRequest(Map(), Map(), Map()), parameter))
  }

  override def endpoints: List[ServerEndpoint[AkkaStreams with capabilities.WebSockets, Future]] =
    List(findAllEndpoint, insertEndpoint, insertManyEndpoint, updateManyEndpoint, deleteManyEndpoint)

}
