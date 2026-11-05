package dev.mongocamp.server.tests

import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.client.api.DocumentSearchApi
import dev.mongocamp.server.test.client.model.SearchRequest
import dev.mongocamp.server.test.MongoCampBaseServerSuite

// the list of documents with the parameters in the request body: same answer as the list with query parameters, also for filters that do not fit in a URL
class DocumentSearchSuite extends MongoCampBaseServerSuite {

  private lazy val searchApi    = DocumentSearchApi()
  private lazy val documentsApi = DocumentApi()

  private val collection = "searchTest"

  private val paginationHeaders = List("x-pagination-count-rows", "x-pagination-rows-per-page", "x-pagination-current-page", "x-pagination-count-pages")

  private def search(
    request: SearchRequest,
    rows: Option[Long] = Some(100),
    page: Option[Long] = Some(1),
    token: String = adminBearerToken,
    name: String = collection
  ) =
    executeRequest(searchApi.searchDocuments("", "", token, "")(name, request, rows, page))

  private def list(
    filter: Option[String],
    sort: Seq[String] = Seq(),
    projection: Seq[String] = Seq(),
    rows: Option[Long] = Some(100),
    page: Option[Long] = Some(1)
  ) =
    executeRequest(documentsApi.listDocuments("", "", adminBearerToken, "")(collection, filter, sort, projection, rows, page))

  test("prepare") {
    val documents: Seq[Map[String, Any]] = (1 to 30).map(
      i => Map("name" -> s"doc$i", "group" -> (if (i % 3 == 0) "a" else "b"), "age" -> i)
    )
    executeRequestToResponse(documentsApi.insertMany("", "", adminBearerToken, "")(collection, documents))
  }

  test("the answer is the one of the list with query parameters") {
    List(
      (Some("group:a"), Seq[String](), Seq[String]()),
      (Some("age:[10 TO 20]"), Seq("-age"), Seq("name", "age")),
      (Some("group:b AND age:[1 TO 9]"), Seq("age"), Seq[String]()),
      (None, Seq("-age"), Seq("name"))
    ).foreach {
      case (filter, sort, projection) =>
        val viaList   = list(filter, sort, projection)
        val viaSearch = search(SearchRequest(filter, sort, projection))
        assertEquals(viaSearch.code.code, 200, s"search for $filter failed: ${viaSearch.body.fold(_.getMessage, _ => "")}")
        assertEquals(viaSearch.body.getOrElse(Nil), viaList.body.getOrElse(Nil), s"filter $filter")
        paginationHeaders.foreach(
          h => assertEquals(viaSearch.header(h), viaList.header(h), s"header $h for $filter")
        )
    }
  }

  test("paging works") {
    val response = search(SearchRequest(None, Seq("age")), Some(10), Some(2))
    assertEquals(response.body.getOrElse(Nil).map(_("age").toString), (11 to 20).map(_.toString).toList)
    assertEquals(response.header("x-pagination-count-rows"), Some("30"))
    assertEquals(response.header("x-pagination-current-page"), Some("2"))
  }

  test("a filter that does not fit in a URL works") {
    // more than 2500 characters, a URL of this length is refused by the server (414)
    val filter = (1 to 400)
      .map(
        i => s"name:nobody$i"
      )
      .mkString("(", " OR ", ") OR group:a")
    assert(filter.length > 2500)
    val response = search(SearchRequest(Some(filter)))
    assertEquals(response.code.code, 200)
    assertEquals(response.body.getOrElse(Nil).size, 10)
  }

  test("an invalid filter answers 400 like the query parameter") {
    val viaBody = search(SearchRequest(Some("name:(")))
    assertEquals(viaBody.code.code, 400)
    assertEquals(viaBody.header("x-error-code"), Some("4001"))
    assertEquals(viaBody.header("x-error-additional-info").exists(!_.isBlank), true)
    val viaQuery = list(Some("name:("))
    assertEquals(viaQuery.code.code, 400)
  }

  test("a blank filter lists all documents") {
    assertEquals(search(SearchRequest(Some("  "))).header("x-pagination-count-rows"), Some("30"))
  }

  test("a user without a grant cannot search, a user with a read grant can") {
    val denied = search(SearchRequest(None), token = testUserBearerToken, name = "other").code.code
    assert(Set(401, 403).contains(denied), s"unexpected status $denied")
    assertEquals(search(SearchRequest(None), token = testUserBearerToken, name = "accounts").code.code, 200)
  }

  test("cleanup") {
    executeRequestToResponse(CollectionApi().deleteCollection("", "", adminBearerToken, "")(collection))
  }
}
