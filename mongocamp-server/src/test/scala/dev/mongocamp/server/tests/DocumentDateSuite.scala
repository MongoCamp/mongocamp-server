package dev.mongocamp.server.tests

import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import dev.mongocamp.server.test.TestServer
import io.circe.parser.parse
import sttp.client3._
import sttp.model.Method
import sttp.model.Uri

// The rule for strings in JSON bodies: a full ISO-8601 instant (with or without milliseconds, `Z` or an offset) is stored as a date, everything else
// (also a date without a time) is stored as string. Numbers that fit into 64 bit are stored as integers.
class DocumentDateSuite extends MongoCampBaseServerSuite {

  private val collection = "dateRuleTest"
  private val dateRange  = "d:[2024-06-01T00:00:00Z TO 2024-06-30T23:59:59.999Z]"

  private def raw(method: Method, path: String, body: Option[String] = None, query: Map[String, String] = Map()) = {
    val request = basicRequest
      .method(method, Uri.unsafeParse(s"${TestServer.serverBaseUrl}/mongodb/collections/$collection$path").addParams(query))
      .auth
      .bearer(adminBearerToken)
      .contentType("application/json")
    val response = body.fold(request)(request.body(_)).send(TestAdditions.backend)
    assertEquals(response.code.code, 200, response.body.fold(identity, identity))
    response.body.fold(identity, identity)
  }

  private def keys(filter: String): Set[String] =
    parse(raw(Method.GET, "/documents", query = Map("filter" -> filter, "rowsPerPage" -> "100"))).toOption.get.asArray.get
      .map(_.hcursor.get[String]("k").toOption.get)
      .toSet

  test("insert dates in different forms") {
    raw(
      Method.PUT,
      "/documents/many/insert",
      Some("""[{"k":"ms","d":"2024-06-15T12:00:00.000Z"},{"k":"nomillis","d":"2024-06-15T12:00:00Z"},{"k":"offset","d":"2024-06-15T14:00:00+02:00"},
          |{"k":"fraction","d":"2024-06-15T12:00:00.5Z"},{"k":"dateonly","d":"2024-06-15"},{"k":"text","d":"not a date"},
          |{"k":"big","n":12345678901234567}]""".stripMargin)
    )
  }

  test("a full instant is a date, with and without milliseconds and with an offset") {
    assertEquals(keys(dateRange), Set("ms", "nomillis", "offset", "fraction"))
  }

  test("a date without a time and other strings stay strings") {
    assertEquals(keys(dateRange).contains("dateonly"), false)
    assertEquals(keys(dateRange).contains("text"), false)
    val dateOnly = raw(Method.GET, "/documents", query = Map("filter" -> "k:dateonly"))
    assertEquals(dateOnly.contains("\"d\":\"2024-06-15\""), true, dateOnly)
  }

  test("a date is written back in a form that is read as date again") {
    val body = raw(Method.GET, "/documents", query = Map("filter" -> "k:nomillis"))
    // Instant.toString drops the zero milliseconds
    assertEquals(body.contains("\"d\":\"2024-06-15T12:00:00Z\""), true, body)
  }

  test("a 64 bit integer is not rounded") {
    val body = raw(Method.GET, "/documents", query = Map("filter" -> "k:big"))
    assertEquals(body.contains("12345678901234567"), true, body)
  }

  // Extended JSON wrappers are read in writes and filters
  test("insert with the Extended JSON wrappers") {
    raw(
      Method.PUT,
      "/documents/many/insert",
      Some("""[{"k":"w1","d":{"$date":"2024-06-16T12:00:00Z"}},{"k":"w2","d":{"$date":1718539200000}},
          |{"k":"w3","d":{"$date":{"$numberLong":"1718539200000"}}},
          |{"k":"w4","n":{"$numberLong":"12345678901234567"},"nested":{"list":[{"$date":"2024-06-17T00:00:00Z"}]}}]""".stripMargin)
    )
  }

  test("$date is stored as a date in all its forms") {
    assertEquals(keys(dateRange).filter(_.startsWith("w")), Set("w1", "w2", "w3"))
  }

  test("$numberLong is stored as a 64 bit integer and the wrapper is not stored") {
    val body = raw(Method.GET, "/documents", query = Map("filter" -> "k:w4"))
    assertEquals(body.contains("\"n\":12345678901234567"), true, body)
    assertEquals(body.contains("$numberLong"), false, body)
    assertEquals(body.contains("$date"), false, body)
  }

  test("$date in a filter") {
    val find = raw(Method.POST, "/documents", Some("""{"filter":{"d":{"$gt":{"$date":"2024-06-16T00:00:00Z"}}},"sort":{},"projection":{}}"""))
    assertEquals(parse(find).toOption.get.asArray.get.map(_.hcursor.get[String]("k").toOption.get).toSet.filter(_.startsWith("w")), Set("w1", "w2", "w3"))
  }

  test("an invalid wrapper answers 400") {
    List("""{"k":"bad","d":{"$date":"yesterday"}}""", """{"k":"bad","n":{"$numberLong":"abc"}}""").foreach(
      body => {
        val response = basicRequest
          .put(Uri.unsafeParse(s"${TestServer.serverBaseUrl}/mongodb/collections/$collection/documents"))
          .auth
          .bearer(adminBearerToken)
          .contentType("application/json")
          .body(body)
          .send(TestAdditions.backend)
        assertEquals(response.code.code, 400, body)
      }
    )
  }

  test("cleanup") {
    raw(Method.DELETE, "")
  }
}
