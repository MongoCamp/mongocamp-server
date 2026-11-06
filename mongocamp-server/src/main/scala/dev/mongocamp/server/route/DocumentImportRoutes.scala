package dev.mongocamp.server.route

import dev.mongocamp.server.ActorHandler
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.auth.AuthorizedCollectionRequest
import dev.mongocamp.server.model.ImportResponse
import dev.mongocamp.server.service.DocumentImportService
import io.circe.generic.auto._
import java.nio.charset.StandardCharsets
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.model.EntityStreamSizeException
import org.apache.pekko.stream.scaladsl.Framing
import org.apache.pekko.stream.scaladsl.Sink
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.stream.Materializer
import org.apache.pekko.util.ByteString
import scala.concurrent.blocking
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import sttp.capabilities.pekko.PekkoStreams
import sttp.capabilities.WebSockets
import sttp.model.Method
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

/** The import of documents from a stream of lines (NDJSON). The body of the request is read as a stream, the memory of the server does not depend on its size. */
object DocumentImportRoutes extends CollectionBaseRoute {

  /** The name of the endpoint: the server lifts the size limit and the request timeout of the HTTP server for it (see `Server`). */
  val EndpointName = "importDocuments"

  /** The longest line, a document in the database has at most 16 MB. */
  val MaxLineBytes = 17 * 1024 * 1024

  val importEndpoint = writeCollectionEndpoint
    .in("documents")
    .in("import")
    .in(streamTextBody(PekkoStreams)(CodecFormat.TextPlain(), Some(StandardCharsets.UTF_8)))
    .out(jsonBody[ImportResponse])
    .summary("Import Documents into a Collection")
    .description(
      "Store the documents of the request body: one JSON document per line (NDJSON, as written by the export). A line is read like a document in the body of an insert (Extended JSON wrappers, dates). The body is read as a stream and the documents are stored in batches without ordering, a line that cannot be stored does not stop the others: the answer counts what was read, stored and failed and names the first failed lines (`errors`, `index` is the number of the line). The size of the body is limited by IMPORT_MAX_BYTES (413 above), there is no time limit."
    )
    .tag(DocumentRoutes.apiName)
    .method(Method.POST)
    .name(EndpointName)
    .serverLogic(
      collectionRequest => body => importDocuments(collectionRequest, body)
    )

  def importDocuments(
    authorizedCollectionRequest: AuthorizedCollectionRequest,
    body: Source[ByteString, Any]
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), ImportResponse]] = {
    implicit val system: ActorSystem  = ActorHandler.requestActorSystem
    implicit val ec: ExecutionContext = ActorHandler.requestExecutionContext
    val materializer                  = Materializer(system)
    val dao                           = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
    val user                          = authorizedCollectionRequest.userInformation
    body
      .via(Framing.delimiter(ByteString("\n"), MaxLineBytes, allowTruncation = true))
      .map(_.utf8String)
      .zipWithIndex
      .map(entry => (entry._2 + 1, entry._1.stripSuffix("\r")))
      .filter(_._2.trim.nonEmpty)
      .map(entry => (entry._1, DocumentImportService.parseLine(entry._1, entry._2)))
      .grouped(DocumentImportService.BatchSize)
      // the database is asked in blocking calls, one batch at a time: the body is only read as fast as the batches are stored
      .via(org.apache.pekko.stream.scaladsl.Flow[Seq[(Long, Either[dev.mongocamp.server.model.InsertError, org.mongodb.scala.bson.collection.immutable.Document])]].foldAsync(DocumentImportService.State())(
        (state, batch) => Future(blocking(DocumentImportService.importBatch(dao, user, batch, state)))
      ))
      .runWith(Sink.headOption)(materializer)
      .map(state => Right(state.getOrElse(DocumentImportService.State()).response))
      .recoverWith {
        case _: EntityStreamSizeException =>
          Future.failed(MongoCampException("the body of the request is greater than IMPORT_MAX_BYTES", StatusCode.PayloadTooLarge))
        case _: Framing.FramingException =>
          Future.failed(MongoCampException(s"a line is longer than ${DocumentImportRoutes.MaxLineBytes} bytes", StatusCode.BadRequest))
      }
  }

  def endpoints: List[ServerEndpoint[PekkoStreams with WebSockets, Future]] = List(importEndpoint)
}
