package dev.mongocamp.server.tests

import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.client.api.DocumentCountApi
import dev.mongocamp.server.test.client.model.UpdateRequest
import dev.mongocamp.server.test.MongoCampBaseServerSuite

// updateMany and deleteMany with the Lucene filter of the list instead of a MongoDB filter in the body
class DocumentManyFilterSuite extends MongoCampBaseServerSuite {

  lazy val documentsApi: DocumentApi  = DocumentApi()
  lazy val countApi: DocumentCountApi = DocumentCountApi()

  private val collection = "manyFilterTest"

  private def count(filter: Option[String] = None): Long =
    executeRequestToResponse(countApi.countDocuments("", "", adminBearerToken, "")(collection, filter)).count

  private def errorCode(response: sttp.client3.Response[_]): Option[String] = response.header("x-error-code")

  test("prepare the documents") {
    val documents: Seq[Map[String, Any]] = (1 to 10).map(i => Map("name" -> s"doc$i", "group" -> (if (i <= 6) "a" else "b"), "age" -> i * 10))
    executeRequestToResponse(documentsApi.insertMany("", "", adminBearerToken, "")(collection, documents))
    assertEquals(count(), 10L)
  }

  test("updateMany with a Lucene filter") {
    val response = executeRequestToResponse(
      documentsApi.updateMany("", "", adminBearerToken, "")(collection, UpdateRequest(Map("flag" -> true), Map()), None, Some("group:a"))
    )
    assertEquals(response.matchedCount, 6L)
    assertEquals(count(Some("flag:true")), 6L)
  }

  test("deleteMany with a Lucene filter") {
    val response = executeRequestToResponse(documentsApi.deleteMany("", "", adminBearerToken, "")(collection, Map(), None, Some("age:[90 TO 100]")))
    assertEquals(response.deletedCount, 2L)
    assertEquals(count(), 8L)
  }

  test("an empty body filter still needs matchAll with a Lucene filter that is absent") {
    val response = executeRequest(documentsApi.deleteMany("", "", adminBearerToken, "")(collection, Map()))
    assertEquals(response.code.code, 400)
    assertEquals(errorCode(response), Some(ErrorCodes.emptyFilter.toString))
    assertEquals(count(), 8L)
  }

  test("the filter in the body and as query are not allowed together") {
    val update = executeRequest(
      documentsApi.updateMany("", "", adminBearerToken, "")(collection, UpdateRequest(Map("x" -> 1), Map("group" -> "a")), None, Some("group:b"))
    )
    assertEquals(update.code.code, 400)
    assertEquals(errorCode(update), Some(ErrorCodes.ambiguousFilter.toString))
    val delete = executeRequest(documentsApi.deleteMany("", "", adminBearerToken, "")(collection, Map("group" -> "a"), None, Some("group:b")))
    assertEquals(delete.code.code, 400)
    assertEquals(errorCode(delete), Some(ErrorCodes.ambiguousFilter.toString))
    assertEquals(count(), 8L)
  }

  test("an invalid Lucene filter answers 400 and changes nothing") {
    assertEquals(executeRequest(documentsApi.deleteMany("", "", adminBearerToken, "")(collection, Map(), None, Some("name:("))).code.code, 400)
    assertEquals(count(), 8L)
  }

  test("a Lucene filter that matches nothing deletes nothing") {
    val response = executeRequestToResponse(documentsApi.deleteMany("", "", adminBearerToken, "")(collection, Map(), None, Some("name:nobody")))
    assertEquals(response.deletedCount, 0L)
    assertEquals(count(), 8L)
  }

  test("the body filter keeps working") {
    val response = executeRequestToResponse(documentsApi.deleteMany("", "", adminBearerToken, "")(collection, Map("group" -> "b")))
    assertEquals(response.deletedCount, 2L)
    assertEquals(count(), 6L)
  }

  test("cleanup") {
    executeRequestToResponse(CollectionApi().deleteCollection("", "", adminBearerToken, "")(collection))
  }
}
