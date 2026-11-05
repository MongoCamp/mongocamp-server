package dev.mongocamp.server.tests

import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.client.api.IndexApi
import dev.mongocamp.server.test.client.model._
import dev.mongocamp.server.test.MongoCampBaseServerSuite

// a request that the database refuses because of its content is answered with a 4xx and the code of the database, not with a 500
class DatabaseErrorStatusSuite extends MongoCampBaseServerSuite {
  private val collection = "errorStatus"
  private lazy val index     = IndexApi()
  private lazy val documents = DocumentApi()
  private lazy val colApi    = CollectionApi()

  private def assertStatus(response: sttp.client3.Response[_], status: Int, databaseCode: Int, hint: String): Unit = {
    assertEquals(response.code.code, status, s"$hint: ${response.body.toString.take(300)}")
    assertEquals(response.header("x-error-code"), Some(databaseCode.toString), hint)
    assertEquals(response.header("x-error-message").exists(!_.isBlank), true, s"$hint: no message")
  }

  private def aggregate(stage: String, value: Any) =
    executeRequest(colApi.aggregate("", "", adminBearerToken, "")(collection, MongoAggregateRequest(Seq(PipelineStage(stage, value)), allowDiskUse = false)))

  test("prepare") {
    executeRequestToResponse(documents.insertMany("", "", adminBearerToken, "")(collection, Seq(Map("a" -> 1, "b" -> "x"), Map("a" -> 1, "b" -> "y"))))
  }

  test("an index without keys is a bad request") {
    assertStatus(executeRequest(index.createIndex("", "", adminBearerToken, "")(collection, IndexCreateRequest(Map(), None))), 400, 67, "empty keys")
  }

  test("a unique index over duplicate data is a conflict without the internal names") {
    val response = executeRequest(
      index.createIndex("", "", adminBearerToken, "")(collection, IndexCreateRequest(Map("a" -> 1), Some(IndexOptionsRequest(Some("dupIdx"), unique = Some(true)))))
    )
    assertEquals(response.code.code, 409)
    assertEquals(response.header("x-error-code"), Some("11000"))
  }

  test("the index of the id cannot be dropped") {
    assertStatus(executeRequest(index.deleteIndex("", "", adminBearerToken, "")(collection, "_id_")), 400, 72, "drop _id_")
  }

  test("an empty field name in the projection or sort is a bad request") {
    assertStatus(executeRequest(documents.listDocuments("", "", adminBearerToken, "")(collection, None, Seq(), Seq(""))), 400, 40352, "projection")
    assertStatus(executeRequest(documents.listDocuments("", "", adminBearerToken, "")(collection, None, Seq(""), Seq())), 400, 40352, "sort")
  }

  test("a pipeline that the database refuses is a bad request") {
    assertStatus(aggregate("$bogus", Map()), 400, 40324, "unknown stage")
    assertStatus(aggregate("$unwind", "a"), 400, 28818, "unwind without $")
    assertStatus(aggregate("$bucketAuto", Map("buckets" -> 2)), 400, 40246, "bucketAuto without groupBy")
    assertStatus(aggregate("$limit", "abc"), 400, 5107201, "limit is not a number")
    assertStatus(aggregate("$group", Map("n" -> Map("$sum" -> 1))), 400, 15955, "group without _id")
    assertStatus(aggregate("$match", Map("a" -> Map("$bogus" -> 1))), 400, 2, "unknown operator")
  }

  test("cleanup") {
    executeRequestToResponse(colApi.deleteCollection("", "", adminBearerToken, "")(collection))
  }
}
