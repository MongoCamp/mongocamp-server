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
}
