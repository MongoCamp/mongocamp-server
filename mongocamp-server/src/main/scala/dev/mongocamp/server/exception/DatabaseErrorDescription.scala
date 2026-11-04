package dev.mongocamp.server.exception

import com.mongodb.ErrorCategory
import com.mongodb.MongoBulkWriteException
import com.mongodb.MongoCommandException
import com.mongodb.MongoConfigurationException
import com.mongodb.MongoException
import com.mongodb.MongoSocketException
import com.mongodb.MongoTimeoutException
import com.mongodb.MongoWriteException
import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import sttp.model.StatusCode

/** Describes errors of the database driver for the clients of the API.
  *
  * The messages of the driver contain the address of the database server and the complete response of the server. That is internal topology, which must not be
  * sent to the clients, so only the message and the code of the database are returned. The full error is logged by the caller.
  */
object DatabaseErrorDescription {

  val ConnectionErrorMessage = "The database could not be reached"

  private val serverAddress    = """\s+on (?:MongoDB )?server\s+\S+?(?=\.\s|\.$|\s|$)""".r
  private val completeResponse = """(?s)\.?\s*The full response is.*$""".r

  @tailrec
  def findDatabaseException(throwable: Throwable, depth: Int = 0): Option[MongoException] = {
    throwable match {
      case null                                            => None
      case mongoException: MongoException                  => Some(mongoException)
      case other if depth >= 10 || other.getCause == other => None
      case other                                           => findDatabaseException(other.getCause, depth + 1)
    }
  }

  private val duplicateKeyCode = 11000

  /** Status code for a database error: a duplicate key is a conflict with the data of the client, everything else stays an internal error. */
  def statusCode(exception: MongoException): StatusCode = {
    val duplicateKey = exception match {
      case write: MongoWriteException     => write.getError.getCategory == ErrorCategory.DUPLICATE_KEY
      case bulk: MongoBulkWriteException  => bulk.getWriteErrors.asScala.exists(_.getCategory == ErrorCategory.DUPLICATE_KEY)
      case command: MongoCommandException => command.getErrorCode == duplicateKeyCode
      case _                              => false
    }
    if (duplicateKey) StatusCode.Conflict else StatusCode.InternalServerError
  }

  def describe(exception: MongoException): ErrorDescription = {
    exception match {
      case command: MongoCommandException =>
        ErrorDescription(command.getErrorCode, command.getErrorMessage)
      case _: MongoTimeoutException | _: MongoSocketException | _: MongoConfigurationException =>
        ErrorDescription(-1, ConnectionErrorMessage)
      case other =>
        ErrorDescription(-1, removeInternalDetails(Option(other.getMessage).getOrElse("Error on database request")))
    }
  }

  def removeInternalDetails(message: String): String = {
    completeResponse.replaceAllIn(serverAddress.replaceAllIn(message, ""), "")
  }
}
