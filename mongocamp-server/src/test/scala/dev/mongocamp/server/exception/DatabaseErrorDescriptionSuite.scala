package dev.mongocamp.server.exception

import com.mongodb.MongoCommandException
import com.mongodb.MongoTimeoutException
import com.mongodb.ServerAddress
import org.bson.BsonDocument

class DatabaseErrorDescriptionSuite extends munit.FunSuite {

  private val internalHost = "db-host.internal"
  private val address      = new ServerAddress(internalHost, 27017)

  private def commandException = new MongoCommandException(
    BsonDocument.parse("""{"ok": 0.0, "errmsg": "the match filter must be an expression in an object", "code": 15959, "codeName": "Location15959"}"""),
    address
  )

  test("a failed command is described by the message and the code of the database") {
    val raw = commandException.getMessage
    assert(raw.contains(internalHost), s"the driver message is expected to contain the server address: $raw")
    val description = DatabaseErrorDescription.describe(commandException)
    assertEquals(description.code, 15959)
    assertEquals(description.msg, "the match filter must be an expression in an object")
  }

  test("a database error wrapped in another exception is found") {
    val wrapped = new RuntimeException("wrapper", new RuntimeException("inner", commandException))
    assertEquals(DatabaseErrorDescription.findDatabaseException(wrapped).map(_.getClass), Some(classOf[MongoCommandException]))
    assertEquals(DatabaseErrorDescription.findDatabaseException(new IllegalStateException("no database error")), None)
  }

  test("connection errors do not reveal the cluster") {
    val timeout = new MongoTimeoutException(
      s"Timed out after 30000 ms while waiting for a server. Client view of cluster state is {servers=[{address=$internalHost:27017}]}"
    )
    val description = DatabaseErrorDescription.describe(timeout)
    assertEquals(description.msg, DatabaseErrorDescription.ConnectionErrorMessage)
    assertEquals(description.msg.contains(internalHost), false)
  }

  test("the address of the server and the complete response are removed from other messages") {
    assertEquals(
      DatabaseErrorDescription.removeInternalDetails(s"Bulk write operation error on server $internalHost:27017. Write errors: [duplicate key]."),
      "Bulk write operation error. Write errors: [duplicate key]."
    )
    assertEquals(
      DatabaseErrorDescription.removeInternalDetails(s"Query failed with error code 2 on server $internalHost:27017. The full response is {\"ok\": 0.0}"),
      "Query failed with error code 2"
    )
    assertEquals(
      DatabaseErrorDescription.removeInternalDetails(s"Write failed: E11000 duplicate key on MongoDB server $internalHost:27017"),
      "Write failed: E11000 duplicate key"
    )
    assertEquals(DatabaseErrorDescription.removeInternalDetails("Index 0 out of bounds"), "Index 0 out of bounds")
  }

  private def command(code: Int, codeName: String) =
    new MongoCommandException(BsonDocument.parse(s"""{"ok": 0.0, "errmsg": "failed", "code": $code, "codeName": "$codeName"}"""), address)

  test("the status follows the code of the database") {
    import sttp.model.StatusCode
    assertEquals(DatabaseErrorDescription.statusCode(command(11000, "DuplicateKey")), StatusCode.Conflict)
    assertEquals(DatabaseErrorDescription.statusCode(command(85, "IndexOptionsConflict")), StatusCode.Conflict)
    assertEquals(DatabaseErrorDescription.statusCode(command(27, "IndexNotFound")), StatusCode.NotFound)
    assertEquals(DatabaseErrorDescription.statusCode(command(26, "NamespaceNotFound")), StatusCode.NotFound)
    assertEquals(DatabaseErrorDescription.statusCode(command(72, "InvalidOptions")), StatusCode.BadRequest)
    assertEquals(DatabaseErrorDescription.statusCode(command(40324, "Location40324")), StatusCode.BadRequest)
    // not known to be the fault of the client
    assertEquals(DatabaseErrorDescription.statusCode(command(91, "ShutdownInProgress")), StatusCode.InternalServerError)
    assertEquals(DatabaseErrorDescription.statusCode(command(13, "Unauthorized")), StatusCode.InternalServerError)
    assertEquals(DatabaseErrorDescription.statusCode(new MongoTimeoutException("timeout")), StatusCode.InternalServerError)
  }

  test("the names of the database and the collection, the ids of an index build and the duplicate values are removed") {
    val indexBuild =
      "Write failed with error code 11000 and error message 'Index build failed: c70f2a29-4a10-449d-bcaa-e86ed870d80a: Collection test.errorProbe ( 1a9f2066-0770-4eb9-9da4-1b333a16056b ) :: caused by :: E11000 duplicate key error collection: test.errorProbe index: dupIdx dup key: { a: 1 }'"
    assertEquals(
      DatabaseErrorDescription.removeInternalDetails(indexBuild),
      "Write failed with error code 11000 and error message 'Index build failed: E11000 duplicate key error, index: dupIdx'"
    )
    assertEquals(
      DatabaseErrorDescription.removeInternalNames("E11000 duplicate key error collection: shop.customers index: _id_ dup key: { _id: \"secret-id\" }"),
      "E11000 duplicate key error, index: _id_"
    )
    assertEquals(
      DatabaseErrorDescription.removeInternalNames("E11000 duplicate key error collection: shop.customers index: email_1 dup key: { email: { a: 1 } }"),
      "E11000 duplicate key error, index: email_1"
    )
    assertEquals(DatabaseErrorDescription.removeInternalNames("Unrecognized pipeline stage name: '$bogus'"), "Unrecognized pipeline stage name: '$bogus'")
  }

  test("the message of a command exception is cleaned too") {
    val exception = new MongoCommandException(
      BsonDocument.parse(
        """{"ok": 0.0, "errmsg": "E11000 duplicate key error collection: shop.customers index: email_1 dup key: { email: \"a@b.c\" }", "code": 11000, "codeName": "DuplicateKey"}"""
      ),
      address
    )
    val message = DatabaseErrorDescription.describe(exception).msg
    assertEquals(message, "E11000 duplicate key error, index: email_1")
  }

  test("the details of a write error are removed, they hold the values that were considered") {
    val message =
      "Write error: WriteError{code=121, message='Document failed validation', details={ \"failingDocumentId\": 1, \"details\": { \"operatorName\": \"$jsonSchema\", \"propertiesNotSatisfied\": [ { \"propertyName\": \"age\", \"details\": [ { \"consideredValue\": \"secret-value-xyz\" } ] } ] } }}"
    val cleaned = DatabaseErrorDescription.removeErrorDetails(message)
    assert(!cleaned.contains("secret-value-xyz"), cleaned)
    assertEquals(cleaned, "Write error: WriteError{code=121, message='Document failed validation'}")
  }

  test("a message without details stays as it is") {
    val message = "Write error: WriteError{code=11000, message='E11000 duplicate key error'}"
    assertEquals(DatabaseErrorDescription.removeErrorDetails(message), message)
  }

  test("several errors lose all their details") {
    val message =
      "[BulkWriteError{index=0, code=121, message='x', details={\"a\": {\"b\": 1}}}, BulkWriteError{index=1, code=121, message='y', details={\"c\": 2}}]"
    assertEquals(
      DatabaseErrorDescription.removeErrorDetails(message),
      "[BulkWriteError{index=0, code=121, message='x'}, BulkWriteError{index=1, code=121, message='y'}]"
    )
  }

  test("a document that the validator refuses is a bad request, also in a bulk write") {
    import com.mongodb.bulk.BulkWriteResult
    import com.mongodb.bulk.BulkWriteError
    import com.mongodb.MongoBulkWriteException
    val error     = new BulkWriteError(121, "Document failed validation", new BsonDocument(), 0)
    val exception = new MongoBulkWriteException(BulkWriteResult.unacknowledged(), java.util.List.of(error), null, address, java.util.Set.of[String]())
    assertEquals(DatabaseErrorDescription.statusCode(exception), sttp.model.StatusCode.BadRequest)
  }
}
