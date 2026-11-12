package dev.mongocamp.server.tests

import dev.mongocamp.server.test.client.api.CollectionAggregateFilterApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.client.model.MongoAggregateRequest
import dev.mongocamp.server.test.client.model.PipelineStage
import dev.mongocamp.server.test.MongoCampBaseServerSuite

// the convention for local-time histograms: `timezone` inside the pipeline ($dateToString, $dateTrunc), no parameter of the route
class AggregationTimezoneSuite extends MongoCampBaseServerSuite {

  private lazy val aggregateApi = CollectionAggregateFilterApi()
  private lazy val documentsApi = DocumentApi()

  private val collection = "aggregationTimezoneTest"

  // 2026-03-29: Europe/Berlin changes from UTC+1 to UTC+2 at 01:00 UTC
  private def bucketBy(format: String, timezone: String) = {
    val dateToString = Map("format" -> format, "date" -> "$at", "timezone" -> timezone)
    List(PipelineStage("group", Map("_id" -> Map("$dateToString" -> dateToString), "n" -> Map("$sum" -> 1))), PipelineStage("sort", Map("_id" -> 1)))
  }

  private def run(pipeline: List[PipelineStage]) =
    executeRequest(aggregateApi.aggregate("", "", adminBearerToken, "")(collection, MongoAggregateRequest(pipeline, allowDiskUse = false)))

  private def buckets(pipeline: List[PipelineStage]): List[(String, String)] = {
    val response = run(pipeline)
    assertEquals(response.code.code, 200, response.body.fold(_.getMessage, _ => ""))
    response.body
      .getOrElse(Nil)
      .map(
        e => (e("_id").toString, e("n").toString)
      )
      .toList
  }

  test("prepare: an ISO instant and a $date wrapper are the same moment") {
    val documents: Seq[Map[String, Any]] = Seq(
      Map("name" -> "iso", "at"     -> "2026-03-29T00:30:00Z"),
      Map("name" -> "wrapper", "at" -> Map("$date" -> "2026-03-29T23:30:00Z")),
      Map("name" -> "iso2", "at"    -> "2026-03-29T23:30:00Z")
    )
    executeRequestToResponse(documentsApi.insertMany("", "", adminBearerToken, "")(collection, documents))
  }

  test("daily buckets in UTC: all documents are on 2026-03-29") {
    assertEquals(buckets(bucketBy("%Y-%m-%d", "UTC")), List(("2026-03-29", "3")))
  }

  test("daily buckets in Europe/Berlin: the DST day moves 23:30Z to the next day") {
    assertEquals(buckets(bucketBy("%Y-%m-%d", "Europe/Berlin")), List(("2026-03-29", "1"), ("2026-03-30", "2")))
  }

  test("hourly buckets show the offset change (+1 before, +2 after 01:00 UTC)") {
    assertEquals(buckets(bucketBy("%H", "Europe/Berlin")), List(("01", "3")))
    assertEquals(buckets(bucketBy("%H", "UTC")), List(("00", "1"), ("23", "2")))
  }

  test("a fixed offset works as timezone") {
    assertEquals(buckets(bucketBy("%Y-%m-%d", "+02:00")), List(("2026-03-29", "1"), ("2026-03-30", "2")))
  }

  test("$dateTrunc with timezone passes the stage guard") {
    val trunc    = Map("$dateTrunc" -> Map("date" -> "$at", "unit" -> "day", "timezone" -> "Europe/Berlin"))
    val response = run(List(PipelineStage("group", Map("_id" -> trunc, "n" -> Map("$sum" -> 1))), PipelineStage("sort", Map("_id" -> 1))))
    assertEquals(response.code.code, 200, response.body.fold(_.getMessage, _ => ""))
    assertEquals(response.body.getOrElse(Nil).map(_("n").toString), List("1", "2"))
  }
}
