package dev.mongocamp.server.tests

import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite

// Probe of the Lucene gaps after mongodb-driver 3.3.7 (plan 05 phase 9). What works is pinned, so a downgrade of the driver fails here; known wrong results
// are `.fail` tests: they pass while the driver is wrong and fail when it is fixed (then move them up and delete the entry in docs/guide/api-behaviour.md).
class LuceneFilterLeftoversSuite extends MongoCampBaseServerSuite {

  private lazy val documentsApi = DocumentApi()
  private val collection        = "luceneLeftovers"

  private def list(filter: String) =
    executeRequest(documentsApi.listDocuments("", "", adminBearerToken, "")(collection, Some(filter), Seq(), Seq(), Some(100), Some(1)))

  private def ids(filter: String): List[String] = {
    val response = list(filter)
    assertEquals(response.code.code, 200, s"filter `$filter`: ${response.body.fold(_.getMessage, _ => "")}")
    response.body.getOrElse(Nil).map(_("id").toString).sorted.toList
  }

  test("prepare documents") {
    val docs: Seq[Map[String, Any]] = Seq(
      Map("id" -> 1, "n" -> 10, "city" -> "a", "word" -> "plain"),
      Map("id" -> 2, "n" -> 10, "city" -> "b", "word" -> "plane"),
      Map("id" -> 3, "n" -> 20, "city" -> "a", "word" -> "plant"),
      Map("id" -> 4, "n" -> 20, "city" -> "b", "word" -> "New York")
    )
    executeRequestToResponse(documentsApi.insertMany("", "", adminBearerToken, "")(collection, docs))
  }

  test("boolean combinations of single terms and of OR") {
    assertEquals(ids("-n:10"), List("3", "4"))
    assertEquals(ids("NOT n:10"), List("3", "4"))
    assertEquals(ids("n:10 AND NOT city:a"), List("2"))
    assertEquals(ids("(n:10 OR n:20) AND city:a"), List("1", "3"))
    assertEquals(ids("-(n:10 OR city:a)"), List("4"))
    assertEquals(ids("n:(10 OR 20)"), List("1", "2", "3", "4"))
  }

  test("negated AND group is evaluated as two negated terms (driver bug, answer should be 2,3,4)".fail) {
    assertEquals(ids("-(n:10 AND city:a)"), List("2", "3", "4"))
  }

  test("text wildcards and phrases") {
    assertEquals(ids("word:pla*"), List("1", "2", "3"))
    assertEquals(ids("word:pla?e"), List("2"))
    assertEquals(ids("""word:"New York""""), List("4"))
    assertEquals(ids("word:New"), Nil)
    assertEquals(ids("city:A"), Nil)
  }

  test("number wildcards find nothing (driver gap, answer should be 1,2 and 3,4)".fail) {
    assertEquals(ids("n:1*"), List("1", "2"))
    assertEquals(ids("n:2?"), List("3", "4"))
  }

  test("range bounds: [ ] inclusive, { } exclusive") {
    assertEquals(ids("n:[10 TO 20}"), List("1", "2"))
    assertEquals(ids("n:{10 TO 20]"), List("3", "4"))
    assertEquals(ids("n:{10 TO 20}"), Nil)
    assertEquals(ids("NOT n:[10 TO 10]"), List("3", "4"))
  }

  test("existence") {
    assertEquals(ids("_exists_:n"), List("1", "2", "3", "4"))
    assertEquals(ids("-_exists_:zzz"), List("1", "2", "3", "4"))
  }

  test("regex, fuzzy and boost answer 400 with the reason") {
    List("word:/pl.*/" -> "RegexpQuery", "word:plai~" -> "FuzzyQuery", "word:plain^2" -> "BoostQuery").foreach {
      case (filter, query) =>
        val response = list(filter)
        assertEquals(response.code.code, 400, filter)
        assertEquals(response.header("x-error-additional-info").exists(_.contains(query)), true, s"reason of `$filter`")
    }
  }

  test("cleanup") {
    executeRequestToResponse(CollectionApi().deleteCollection("", "", adminBearerToken, "")(collection))
  }
}
