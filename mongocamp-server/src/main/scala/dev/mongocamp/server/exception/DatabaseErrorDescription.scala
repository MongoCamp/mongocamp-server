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

  // the messages of the database name the database and the collection, the id of an index build and the value of the duplicate key, that is data of
  // the server and of other users
  private val uuid                = """[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}"""
  private val indexBuildPrefix    = s"""(Index build failed): $uuid: Collection \\S+ \\(\\s*$uuid\\s*\\) :: caused by :: """.r
  private val duplicateKey        = """(?s)(E11000 duplicate key error) collection: \S+ index: (\S+)(?: dup key: \{.*\})?""".r
  private val remainingIdentifier = s"""\\s*\\(?\\s*$uuid\\s*\\)?""".r

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

  // Error codes of the database for requests that the client got wrong. Everything else stays an internal error, because it is not known to be the fault of the
  // client (a shutdown, a lost primary, a resource limit).
  private val notFoundCodes = Set(
    26, // NamespaceNotFound
    27  // IndexNotFound
  )
  private val conflictCodes = Set(
    duplicateKeyCode,
    11001, // duplicate key on update
    12582, // duplicate key (legacy)
    68,    // NamespaceExists
    85,    // IndexOptionsConflict
    86     // IndexKeySpecsConflict
  )
  private val badRequestCodes = Set(
    2,  // BadValue
    9,  // FailedToParse
    14, // TypeMismatch
    40, // ConflictingUpdateOperators
    52, // DollarPrefixedFieldName
    67, // CannotCreateIndex
    72, // InvalidOptions
    73  // InvalidNamespace
  )
  // the server reports the checks of a stage or an expression ("a group specification must include an _id") with a code of the form Location<number>
  private val locationCodeName = """Location\d+""".r

  private def isBadRequest(code: Int, codeName: String): Boolean =
    badRequestCodes.contains(code) || (codeName != null && locationCodeName.matches(codeName))

  /** Status code for a database error: a duplicate key or a conflicting index is a conflict with the data of the client, a missing index or collection is
    * not found, a request that the database cannot parse or execute because of its content is a bad request, everything else stays an internal error.
    */
  def statusCode(exception: MongoException): StatusCode = {
    val bulkDuplicate = exception match {
      case write: MongoWriteException    => write.getError.getCategory == ErrorCategory.DUPLICATE_KEY
      case bulk: MongoBulkWriteException => bulk.getWriteErrors.asScala.exists(_.getCategory == ErrorCategory.DUPLICATE_KEY)
      case _                             => false
    }
    val (code, codeName) = exception match {
      case command: MongoCommandException => (command.getErrorCode, command.getErrorCodeName)
      case other                          => (other.getCode, null)
    }
    if (bulkDuplicate || conflictCodes.contains(code)) StatusCode.Conflict
    else if (notFoundCodes.contains(code)) StatusCode.NotFound
    else if (isBadRequest(code, codeName)) StatusCode.BadRequest
    else StatusCode.InternalServerError
  }

  def describe(exception: MongoException): ErrorDescription = {
    exception match {
      case command: MongoCommandException =>
        ErrorDescription(command.getErrorCode, removeInternalNames(command.getErrorMessage))
      case _: MongoTimeoutException | _: MongoSocketException | _: MongoConfigurationException =>
        ErrorDescription(-1, ConnectionErrorMessage)
      case other =>
        ErrorDescription(if (other.getCode > 0) other.getCode else -1, removeInternalDetails(Option(other.getMessage).getOrElse("Error on database request")))
    }
  }

  def removeInternalDetails(message: String): String = {
    removeInternalNames(completeResponse.replaceAllIn(serverAddress.replaceAllIn(message, ""), ""))
  }

  /** Removes the names of the database and the collection, the ids of index builds and the values of a duplicate key from a message of the database. */
  def removeInternalNames(message: String): String = {
    val withoutBuild     = indexBuildPrefix.replaceAllIn(message, m => scala.util.matching.Regex.quoteReplacement(s"${m.group(1)}: "))
    val withoutDuplicate = duplicateKey.replaceAllIn(withoutBuild, m => scala.util.matching.Regex.quoteReplacement(s"${m.group(1)}, index: ${m.group(2)}"))
    remainingIdentifier.replaceAllIn(withoutDuplicate, "")
  }
}
