package dev.mongocamp.server.tests

import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.client.api.DocumentCountApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite

class DocumentCountSuite extends MongoCampBaseServerSuite {

  lazy val documentsApi: DocumentApi   = DocumentApi()
  lazy val countApi: DocumentCountApi = DocumentCountApi()

  private val collection = "countTest"

  private def count(filter: Option[String] = None, token: String = adminBearerToken, name: String = collection) =
    countApi.countDocuments("", "", token, "")(name, filter)

  private def countOk(filter: Option[String] = None): Long = {
    val response = executeRequest(count(filter))
    assertEquals(response.code.code, 200, s"count for `$filter` failed: ${response.body.fold(_.getMessage, _ => "")}")
    response.body.fold(error => fail(error.getMessage), _.count)
  }

  test("prepare the documents") {
    val documents: Seq[Map[String, Any]] =
      Seq(
        Map("_id" -> "count", "name" -> "literal id", "age" -> 10),
        Map("name" -> "a", "age" -> 20),
        Map("name" -> "b", "age" -> 30),
        Map("name" -> "c", "age" -> 40),
        Map("name" -> "d", "age" -> 50)
      )
    executeRequestToResponse(documentsApi.insertMany("", "", adminBearerToken, "")(collection, documents))
  }

  test("count all documents without a filter") {
    assertEquals(countOk(), 5L)
  }

  test("count the documents that match a Lucene filter") {
    assertEquals(countOk(Some("name:a")), 1L)
    assertEquals(countOk(Some("age:[20 TO 40]")), 3L)
    assertEquals(countOk(Some("name:nobody")), 0L)
  }

  test("an invalid filter answers 400") {
    assertEquals(executeRequest(count(Some("name:("))).code.code, 400)
  }

  test("a document with the id count is reached by the find routes, the path count is the count route") {
    val byFilter = executeRequest(documentsApi.listDocuments("", "", adminBearerToken, "")(collection, Some("_id:count"), Seq(), Seq(), Some(10), Some(1)))
    assertEquals(byFilter.body.getOrElse(Nil).map(_("name")), List("literal id"))
    // GET .../documents/count is the count of the collection, not the document with that id
    val single = executeRequest(documentsApi.getDocument("", "", adminBearerToken, "")(collection, "count")).body.getOrElse(Map())
    assertEquals(single.get("count").map(_.toString), Some("5"))
    assertEquals(single.contains("name"), false)
  }

  test("a user without a grant for the collection cannot count") {
    val code = executeRequest(count(token = testUserBearerToken, name = "other")).code.code
    assert(Set(401, 403).contains(code), s"unexpected status $code")
  }

  test("a user with a read grant can count") {
    val response = executeRequest(count(token = testUserBearerToken, name = "accounts"))
    assertEquals(response.code.code, 200)
  }

  test("cleanup") {
    executeRequestToResponse(CollectionApi().deleteCollection("", "", adminBearerToken, "")(collection))
  }
}
