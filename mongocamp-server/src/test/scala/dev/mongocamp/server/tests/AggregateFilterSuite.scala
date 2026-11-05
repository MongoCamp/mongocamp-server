package dev.mongocamp.server.tests

import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.test.client.api.CollectionAggregateFilterApi
import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.client.model.MongoAggregateRequest
import dev.mongocamp.server.test.client.model.PipelineStage
import dev.mongocamp.server.test.MongoCampBaseServerSuite

// aggregate what the list shows: the Lucene filter of the list as the first stage of the pipeline
class AggregateFilterSuite extends MongoCampBaseServerSuite {

  private lazy val aggregateApi = CollectionAggregateFilterApi()
  private lazy val documentsApi = DocumentApi()

  private val collection = "aggregateFilterTest"

  private val groupByType = List(
    PipelineStage("group", Map("_id" -> "$type", "n" -> Map("$sum" -> 1))),
    PipelineStage("sort", Map("_id" -> 1))
  )

  private def run(pipeline: List[PipelineStage], filter: Option[String], rows: Option[Long] = Some(100), name: String = collection, token: String = adminBearerToken) =
    executeRequest(aggregateApi.aggregate("", "", token, "")(name, MongoAggregateRequest(pipeline, allowDiskUse = false), filter, rows, Some(1)))

  test("prepare") {
    val documents: Seq[Map[String, Any]] =
      (1 to 30).map(i => Map("name" -> s"doc$i", "type" -> (if (i <= 10) "a" else if (i <= 20) "b" else "c"), "age" -> i))
    executeRequestToResponse(documentsApi.insertMany("", "", adminBearerToken, "")(collection, documents))
  }

  test("without a filter nothing changes") {
    val response = run(groupByType, None)
    assertEquals(response.code.code, 200)
    assertEquals(response.body.getOrElse(Nil).map(_("n").toString), List("10", "10", "10"))
  }

  test("the filter is applied before the pipeline, like a match stage") {
    val viaFilter = run(groupByType, Some("age:[5 TO 15]"))
    val viaMatch  = run(PipelineStage("match", Map("age" -> Map("$gte" -> 5, "$lte" -> 15))) :: groupByType, None)
    assertEquals(viaFilter.code.code, 200, viaFilter.body.fold(_.getMessage, _ => ""))
    assertEquals(viaFilter.body.getOrElse(Nil), viaMatch.body.getOrElse(Nil))
    assertEquals(viaFilter.body.getOrElse(Nil).map(e => (e("_id").toString, e("n").toString)), List(("a", "6"), ("b", "5")))
  }

  test("the pagination counts the result of the filtered pipeline") {
    val response = run(List(PipelineStage("sort", Map("age" -> 1))), Some("type:b"), Some(4))
    assertEquals(response.header("x-pagination-count-rows"), Some("10"))
    assertEquals(response.header("x-pagination-count-pages"), Some("3"))
    assertEquals(response.body.getOrElse(Nil).size, 4)
  }

  test("a filter that matches nothing gives an empty result") {
    assertEquals(run(groupByType, Some("name:nobody")).body.getOrElse(Nil).size, 0)
  }

  test("an invalid filter is a bad request") {
    assertEquals(run(groupByType, Some("name:(")).code.code, 400)
  }

  test("a pipeline that has to start with a special stage cannot have a filter") {
    val geoNear = PipelineStage("geoNear", Map("near" -> Map("type" -> "Point", "coordinates" -> List(0, 0)), "distanceField" -> "distance", "spherical" -> true))
    val response = run(List(geoNear), Some("type:company"), name = "geodata:locations")
    assertEquals(response.code.code, 400)
    assertEquals(response.header("x-error-code"), Some(ErrorCodes.filterNotSupported.toString))
    // without the filter the pipeline works
    assertEquals(run(List(geoNear, PipelineStage("limit", 1)), None, name = "geodata:locations").code.code, 200)
  }

  test("the filter does not widen what a user may read") {
    val denied = run(groupByType, Some("type:a"), name = "other", token = testUserBearerToken).code.code
    assert(Set(401, 403).contains(denied), s"unexpected status $denied")
  }

  test("cleanup") {
    executeRequestToResponse(CollectionApi().deleteCollection("", "", adminBearerToken, "")(collection))
  }
}
