package dev.mongocamp.server.tests

import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite

class LuceneFilterSuite extends MongoCampBaseServerSuite {

  lazy val documentsApi: DocumentApi = DocumentApi()

  private val collection = "luceneFilterTest"
  private val cities     = List("x:y", "a/b", "New York", "plain")

  private def list(filter: String) =
    executeRequest(documentsApi.listDocuments("", "", adminBearerToken, "")(collection, Some(filter), Seq(), Seq(), Some(100), Some(1)))

  test("prepare documents") {
    val docs: Seq[Map[String, Any]] = cities.zipWithIndex.map(c => Map("city" -> c._1, "n" -> (c._2 * 10)))
    executeRequestToResponse(documentsApi.insertMany("", "", adminBearerToken, "")(collection, docs))
  }

  test("quoted values with a colon or a slash are matched") {
    List("x:y", "a/b").foreach(
      value => {
        val response = list(s"""city:"$value"""")
        assertEquals(response.code.code, 200, s"filter for `$value` failed: ${response.body.fold(identity, _ => "")}")
        assertEquals(response.body.getOrElse(Nil).map(_("city")), List(value))
      }
    )
  }

  test("a filter that the driver cannot convert answers 400 with a message") {
    val response = list("n:[5 TO *]")
    assertEquals(response.code.code, 400)
    assertEquals(response.header("x-error-message").exists(!_.isBlank), true)
    assertEquals(response.header("x-error-additional-info").exists(!_.isBlank), true, "the reason is missing")
  }

  test("an invalid filter answers 400") {
    assertEquals(list("city:(").code.code, 400)
  }

  test("cleanup") {
    executeRequestToResponse(CollectionApi().deleteCollection("", "", adminBearerToken, "")(collection))
  }
}
