package dev.mongocamp.server.service

import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.model.InsertError
import dev.mongocamp.server.service.DocumentImportService._
import org.bson.BsonType

class DocumentImportServiceSuite extends munit.FunSuite {

  private def document(line: String) = parseLine(1, line).getOrElse(fail(s"not a document: ${parseLine(1, line)}")).toBsonDocument

  private def error(number: Long, line: String): InsertError = parseLine(number, line).left.getOrElse(fail(s"no error for $line"))

  test("a line is a document, a document without an id gets one") {
    val withId = document("""{"_id": "a", "n": 1}""")
    assertEquals(withId.getString("_id").getValue, "a")
    // a plain JSON number is read as a 64 bit integer, like in the body of an insert
    assertEquals(withId.getInt64("n").getValue, 1L)
    val withoutId = document("""{"n": 1}""")
    assertEquals(withoutId.get("_id").getBsonType, BsonType.OBJECT_ID)
  }

  test("the values are read like in the body of an insert: wrappers, dates and the text marker") {
    val doc = document(
      """{"_id": {"$numberInt": "7"}, "dec": {"$numberDecimal": "10.25"}, "long": {"$numberLong": "9007199254740993"}, "date": "2024-06-15T12:00:00Z", "text": {"$string": "2024-06-15T12:00:00Z"}, "list": [{"$oid": "507f1f77bcf86cd799439011"}]}"""
    )
    assertEquals(doc.get("_id").getBsonType, BsonType.INT32)
    assertEquals(doc.get("dec").getBsonType, BsonType.DECIMAL128)
    assertEquals(doc.get("long").getBsonType, BsonType.INT64)
    assertEquals(doc.get("date").getBsonType, BsonType.DATE_TIME)
    assertEquals(doc.get("text").getBsonType, BsonType.STRING)
    assertEquals(doc.getArray("list").get(0).getBsonType, BsonType.OBJECT_ID)
  }

  test("a line that is not a document is an error with the number of the line") {
    val notJson = error(7, "{not json")
    assertEquals((notJson.index, notJson.code), (7, ErrorCodes.invalidImportLine))
    assert(notJson.message.contains("not valid JSON"), notJson.message)
    assertEquals(error(3, "[1, 2]").message, "the line has to be a JSON object")
    assertEquals(error(4, "\"text\"").index, 4)
    assertEquals(error(5, "42").index, 5)
  }

  test("a wrapper with a content that is not valid is an error of the line") {
    val bad = error(9, """{"n": {"$numberInt": "12.5"}}""")
    assertEquals(bad.index, 9)
    assert(bad.message.contains("$numberInt"), bad.message)
  }

  test("the errors that are listed are limited, the number of failed lines is not") {
    val errors = (1 to MaxReportedErrors + 50).map(i => InsertError(i, ErrorCodes.invalidImportLine, "x"))
    val state  = State(failed = errors.size).withErrors(errors)
    assertEquals(state.errors.size, MaxReportedErrors)
    assertEquals(state.errorsTruncated, true)
    assertEquals(state.response.failed, (MaxReportedErrors + 50).toLong)
    val small = State().withErrors(errors.take(3))
    assertEquals((small.errors.size, small.errorsTruncated), (3, false))
  }
}
