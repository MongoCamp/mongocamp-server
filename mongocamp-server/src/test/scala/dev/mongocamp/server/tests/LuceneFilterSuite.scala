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
    val docs: Seq[Map[String, Any]] = cities.zipWithIndex.map(
      c => Map("city" -> c._1, "n" -> (c._2 * 10))
    )
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

  test("open ranges and comparison operators are converted") {
    List("n:[5 TO *]" -> 3, "n:[* TO 15]" -> 2, "n:>=20" -> 2, "n:>20" -> 1, "_exists_:n" -> 4).foreach(
      filterAndCount => {
        val response = list(filterAndCount._1)
        assertEquals(response.code.code, 200, s"filter `${filterAndCount._1}` failed: ${response.body.fold(identity, _ => "")}")
        assertEquals(response.body.getOrElse(Nil).size, filterAndCount._2, s"filter `${filterAndCount._1}`")
      }
    )
  }

  test("a filter that the driver cannot convert answers 400 with a message") {
    val response = list("city:plai~")
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
