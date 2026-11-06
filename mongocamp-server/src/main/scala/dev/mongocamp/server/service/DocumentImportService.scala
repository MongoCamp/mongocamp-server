package dev.mongocamp.server.service

import com.mongodb.MongoBulkWriteException
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.driver.mongodb.MongoDAO
import dev.mongocamp.server.converter.CirceSchema
import dev.mongocamp.server.converter.MongoCampBsonConverter
import dev.mongocamp.server.event.document.CreateDocumentEvent
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.exception.DatabaseErrorDescription
import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.auth.UserInformation
import dev.mongocamp.server.model.ImportResponse
import dev.mongocamp.server.model.InsertError
import dev.mongocamp.server.model.InsertResponse
import io.circe.parser.parse
import org.bson.types.ObjectId
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.InsertManyOptions
import scala.jdk.CollectionConverters._

/** The import of documents, one JSON document per line (NDJSON): every line is read like a document in the body of an insert (Extended JSON wrappers, dates),
  * the documents are stored in batches without ordering, so that a line that cannot be stored does not stop the others. What fails is reported with the number
  * of the line.
  */
object DocumentImportService extends CirceSchema {

  val BatchSize         = 1000
  val MaxReportedErrors = 1000

  case class State(read: Long = 0, inserted: Long = 0, failed: Long = 0, errors: Vector[InsertError] = Vector(), errorsTruncated: Boolean = false) {
    def withErrors(newErrors: Seq[InsertError]): State = {
      val room = MaxReportedErrors - errors.size
      copy(errors = errors ++ newErrors.take(room), errorsTruncated = errorsTruncated || newErrors.size > room)
    }

    def response: ImportResponse = ImportResponse(read, inserted, failed, errors.toList, errorsTruncated)
  }

  private def lineError(number: Long, message: String) = InsertError(number.toInt, ErrorCodes.invalidImportLine, message)

  /** The document of a line. A document without an `_id` gets an ObjectId here, so that the events can name the ids of what was stored. */
  def parseLine(number: Long, text: String): Either[InsertError, Document] =
    parse(text) match {
      case Left(failure) => Left(lineError(number, "the line is not valid JSON: " + failure.message.linesIterator.next().take(160)))
      case Right(json) if !json.isObject => Left(lineError(number, "the line has to be a JSON object"))
      case Right(json) =>
        try {
          val map      = MongoCampBsonConverter.convertFields(decodeFromJson(json).asInstanceOf[Map[String, Any]])
          val document = documentFromScalaMap(map)
          Right(if (document.contains("_id")) document else document + ("_id" -> new ObjectId()))
        }
        catch {
          case e: MongoCampException => Left(lineError(number, e.getMessage))
        }
    }

  /** Stores a batch of the numbered lines. */
  def importBatch(dao: MongoDAO[Document], user: UserInformation, batch: Seq[(Long, Either[InsertError, Document])], state: State): State = {
    val parseErrors = batch.collect { case (_, Left(error)) => error }
    val valid       = batch.collect { case (number, Right(document)) => (number, document) }
    val dbErrors =
      if (valid.isEmpty) Seq()
      else
        try {
          dao.insertMany(valid.map(_._2), new InsertManyOptions().ordered(false)).result()
          Seq()
        }
        catch {
          case ex: Exception =>
            DatabaseErrorDescription.findDatabaseException(ex) match {
              case Some(bulk: MongoBulkWriteException) if !bulk.getWriteErrors.isEmpty =>
                bulk.getWriteErrors.asScala.toList.map(
                  error => (valid(error.getIndex)._1, InsertError(valid(error.getIndex)._1.toInt, error.getCode, DatabaseErrorDescription.removeInternalNames(error.getMessage)))
                )
              case _ => throw ex
            }
        }
    val failedLines = dbErrors.map(_._1).toSet
    val storedIds   = valid.filterNot(entry => failedLines.contains(entry._1)).map(entry => MongoCampBsonConverter.idToString(entry._2.get("_id").get))
    if (storedIds.nonEmpty) {
      EventSystem.publish(CreateDocumentEvent(user, InsertResponse(wasAcknowledged = true, storedIds.toList, Some(storedIds.size.toLong), Some(List()))))
    }
    val allErrors = (parseErrors ++ dbErrors.map(_._2)).sortBy(_.index)
    state
      .copy(read = state.read + batch.size, inserted = state.inserted + storedIds.size, failed = state.failed + allErrors.size)
      .withErrors(allErrors)
  }
}
