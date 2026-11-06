package dev.mongocamp.server.route

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.converter.ExtendedJsonWriter
import dev.mongocamp.server.converter.LuceneQueryTools
import dev.mongocamp.server.converter.MongoCampBsonConverter
import dev.mongocamp.server.converter.MongoCampBsonConverter.convertFields
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.auth.AuthorizedCollectionRequest
import dev.mongocamp.server.route.parameter.ExtendedJsonFunctions
import dev.mongocamp.server.service.DocumentExportService
import java.nio.charset.StandardCharsets
import org.apache.lucene.search.Query
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import org.mongodb.scala.bson.collection.immutable.Document
import scala.concurrent.Future
import sttp.capabilities.pekko.PekkoStreams
import sttp.capabilities.WebSockets
import sttp.model.Method
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.server.ServerEndpoint

/** The documents of a collection as a download: a stream that is written while the database delivers the documents. */
object DocumentExportRoutes extends CollectionBaseRoute {

  /** The number of documents that are looked at to find the columns of a csv export, when no `projection` names them. */
  val ColumnSample = 1000

  val exportEndpoint = readCollectionEndpoint
    .in("documents")
    .in("export")
    .in(query[String]("format").description("ndjson (one JSON document per line), json (one array) or csv").default("ndjson"))
    .in(
      query[Option[Query]]("filter")(using Codec.listHeadOption(LuceneQueryTools.filterCodec))
        .description("Filter Query like on the list of documents, by default all documents are exported")
        .default(None)
    )
    .in(query[List[String]]("sort").description("Sorting like on the list of documents, a leading - sorts descending").default(List.empty))
    .in(query[List[String]]("projection").description("The fields to export, for csv they are the columns in this order").default(List.empty))
    .in(query[Option[Int]]("limit").description("The greatest number of documents, by default all").default(None))
    .in(ExtendedJsonFunctions.extendedJsonParameter)
    .out(header[String]("Content-Disposition"))
    .out(streamTextBody(PekkoStreams)(CodecFormat.TextPlain(), Some(StandardCharsets.UTF_8)))
    .summary("Export Documents of a Collection")
    .description(
      s"Download the documents of a Collection as a stream: ndjson (one JSON document per line), json (one array) or csv (a header line, nested values as JSON, a text with a comma, a quote or a line break in quotes). The documents are written while the database delivers them, the memory of the server does not depend on the size of the Collection. The columns of a csv export are the fields of `projection`, by default the fields of the first $ColumnSample documents. With extendedJson the types of the values are explicit. A failure while the stream is running ends the stream, the status is 200 by then."
    )
    .tag(DocumentRoutes.apiName)
    .method(Method.GET)
    .name("exportDocuments")
    .serverLogic(
      collectionRequest => parameter => exportDocuments(collectionRequest, parameter)
    )

  def exportDocuments(
    authorizedCollectionRequest: AuthorizedCollectionRequest,
    parameter: (String, Option[Query], List[String], List[String], Option[Int], Option[String])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (String, Source[ByteString, Any])]] =
    Future.successful(Right {
      val (formatName, luceneFilter, sortFields, projectionFields, limit, extendedJson) = parameter
      val format = DocumentExportService.parseFormat(formatName)
      val mode   = ExtendedJsonWriter.parseMode(extendedJson)
      limit.foreach(
        value => if (value <= 0) throw MongoCampException.badRequestException("limit has to be greater than 0")
      )
      val filter     = convertFields(LuceneQueryTools.toFilterMap(luceneFilter))
      val sort       = sortFields.map(field => if (field.startsWith("-")) (field.substring(1) -> -1) else (field -> 1)).toMap[String, Any]
      val projection = projectionFields.map(field => (field -> 1)).toMap[String, Any]
      val dao        = MongoDatabase.databaseProvider.dao(authorizedCollectionRequest.collection)
      val columns =
        if (format != DocumentExportService.Csv) List()
        else if (projectionFields.nonEmpty) projectionFields
        else {
          val names = dao.columnNames(ColumnSample)
          if (names.contains("_id")) "_id" :: names.filterNot(_ == "_id") else names
        }
      // the observable of the driver is a reactive streams publisher: the documents are requested as fast as the client reads them, the cursor is closed when the stream ends or the client is gone
      val documents = Source.fromPublisher(dao.find(filter, sort, projection, limit.getOrElse(0), 0)).map(document => MongoCampBsonConverter.documentToMap(document, mode))
      val fileName  = authorizedCollectionRequest.collection.replaceAll("[^A-Za-z0-9._-]", "_") + "." + format.extension
      (s"""attachment; filename="$fileName"""", DocumentExportService.render(format, documents, columns).map(text => ByteString(text, StandardCharsets.UTF_8)))
    })

  def endpoints: List[ServerEndpoint[PekkoStreams with WebSockets, Future]] = List(exportEndpoint)
}
