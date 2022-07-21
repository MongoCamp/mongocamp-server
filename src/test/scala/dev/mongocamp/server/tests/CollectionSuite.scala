package dev.mongocamp.server.tests

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.client.api.{CollectionApi, DatabaseApi}
import dev.mongocamp.server.client.model.{MongoAggregateRequest, PipelineStage}
import dev.mongocamp.server.database.MongoDatabase

class CollectionSuite extends BaseSuite {

  val collectionApi: CollectionApi = CollectionApi()
  val databaseApi: DatabaseApi     = DatabaseApi()

  test("list all collections as admin") {
    val response = executeRequestToResponse(collectionApi.listCollections("", adminBearerToken)())
    assertEquals(
      response,
      List(
        "accounts",
        "admin-test",
        "mc_jobs",
        "mc_request_logging",
        "mc_roles",
        "mc_token_cache",
        "mc_users",
        "pokemon",
        "sample-files.chunks",
        "sample-files.files",
        "test",
        "users"
      )
    )
    assertEquals(response.size, 12)

  }

  test("collection status accounts as admin") {
    val response = executeRequestToResponse(collectionApi.getCollectionInformation("", adminBearerToken)("accounts"))
    assertEquals(response.size, 10105.0)
    assertEquals(response.count, 100)
  }

  test("delete collection infos as admin") {
    MongoDatabase.databaseProvider.dao("deleteTest").createIndexForField("index_for_test").result()
    val response = executeRequestToResponse(collectionApi.getCollectionInformation("", adminBearerToken)("deleteTest"))
    assertEquals(response.ns, "test.deleteTest")
    val deleteResponse = executeRequestToResponse(collectionApi.deleteCollection("", adminBearerToken)("deleteTest"))
    assertEquals(deleteResponse.value, true)
  }

  test("clear collection infos as admin") {
    val collection = MongoDatabase.databaseProvider.dao("otherDB:collectionName")
    collection.insertOne(Map("key" -> "value")).result()
    assertEquals(collection.count().result(), 1L)
    val deleteResponseResult = executeRequestToResponse(collectionApi.clearCollection("", adminBearerToken)("otherDB:collectionName"))
    assertEquals(deleteResponseResult.value, true)
    assertEquals(collection.count().result(), 0L)
    val databaseDelete = executeRequestToResponse(databaseApi.deleteDatabase("", adminBearerToken)("otherDB"))
    assertEquals(databaseDelete.value, true)
  }

  test("distinct on collection as admin") {
    val distinct = executeRequestToResponse(collectionApi.distinct("", adminBearerToken)("users", "numberrange"))
    assertEquals(distinct.size, 11)
    assertEquals(distinct.sortBy(f => f.toString.toLong), List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
  }

  test("aggregation on collection as admin") {
    val geoNearMap: Map[String, Any] = Map(
      "near"          -> Map("type" -> "Point", "coordinates" -> List(-113.4483806208, 44.0010717100)),
      "distanceField" -> "dist.calculated",
      "maxDistance"   -> 200,
      "query"         -> Map("type" -> "company"),
      "includeLocs"   -> "dist.location",
      "spherical"     -> true
    )
    val aggregateRequest = MongoAggregateRequest(
      List(
        PipelineStage("geoNear", geoNearMap)
      ),
      allowDiskUse = true
    )
    val aggreationResult = executeRequestToResponse(collectionApi.aggregate("", adminBearerToken)("geodata:locations", aggregateRequest))
    assertEquals(aggreationResult.size, 1)
    val headResponse = aggreationResult.head
    assertEquals(headResponse("name").toString, "Mollis Dui Associates")
    assertEquals(headResponse("type").toString, "company")
  }

  test("fields on collection as admin") {
    val distinct = executeRequestToResponse(collectionApi.getCollectionFields("", adminBearerToken)("geodata:locations"))
    assertEquals(distinct.size, 5)
    assertEquals(distinct.sorted, List("_id", "checkedAt", "geodata", "name", "type"))
  }

  test("list all collections as user") {
    val response = executeRequestToResponse(collectionApi.listCollections("", testUserBearerToken)())
    assertEquals(response.size, 3)
    assertEquals(response, List("accounts", "test", "users"))
  }

  test("collection status accounts as user") {
    val response = executeRequestToResponse(collectionApi.getCollectionInformation("", testUserBearerToken)("accounts"))
    assertEquals(response.size, 10105.0)
    assertEquals(response.count, 100)
  }

  test("collection status companies as user") {
    val responseResult = executeRequest(collectionApi.getCollectionInformation("", testUserBearerToken)("companies"))
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for collection")
  }

  test("distinct on collection as user") {
    val distinct = executeRequestToResponse(collectionApi.distinct("", testUserBearerToken)("users", "numberrange"))
    assertEquals(distinct.size, 11)
    assertEquals(distinct.sortBy(f => f.toString.toLong), List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
  }

  test("delete collection infos as user") {
    MongoDatabase.databaseProvider.dao("deleteTest").createIndexForField("index_for_test").result()
    val response = executeRequestToResponse(collectionApi.getCollectionInformation("", testUserBearerToken)("deleteTest"))
    assertEquals(response.ns, "test.deleteTest")
    val deleteResponseResult = executeRequest(collectionApi.deleteCollection("", testUserBearerToken)("deleteTest"))
    assertEquals(deleteResponseResult.code.code, 401)
    assertEquals(deleteResponseResult.header("x-error-message").isDefined, true)
    assertEquals(deleteResponseResult.header("x-error-message").get, "user not authorized for collection")
  }

  test("aggregation on collection as user") {
    val geoNearMap: Map[String, Any] = Map(
      "near"          -> Map("type" -> "Point", "coordinates" -> List(-113.4483806208, 44.0010717100)),
      "distanceField" -> "dist.calculated",
      "maxDistance"   -> 200,
      "query"         -> Map("type" -> "company"),
      "includeLocs"   -> "dist.location",
      "spherical"     -> true
    )
    val aggregateRequest = MongoAggregateRequest(
      List(
        PipelineStage("geoNear", geoNearMap)
      ),
      allowDiskUse = true
    )
    val aggreationResult = executeRequestToResponse(collectionApi.aggregate("", testUserBearerToken)("geodata:locations", aggregateRequest))
    assertEquals(aggreationResult.size, 1)
    val headResponse = aggreationResult.head
    assertEquals(headResponse("name").toString, "Mollis Dui Associates")
    assertEquals(headResponse("type").toString, "company")
  }

  test("check schema of `users` collection") {
    val schemaAnalysis = executeRequestToResponse(collectionApi.getSchemaAnalysis("", adminBearerToken)("users"))
    assertEquals(schemaAnalysis.count, 1000L)
    assertEquals(schemaAnalysis.sample, 1000L)
    assertEquals(schemaAnalysis.percentageOfAnalysed, 1.0)
    val emailField = schemaAnalysis.fields.get.filter(f => f.name.equalsIgnoreCase("email"))
    assertEquals(emailField.nonEmpty, true)
    assertEquals(emailField.head.percentageOfParent, 1.0)
  }

  test("check schema of `pokemon` collection") {
    val jsonSchema = executeRequestToResponse(collectionApi.getJsonSchema("", adminBearerToken)("pokemon"))
    assertEquals(jsonSchema.$schema, "https://json-schema.org/draft/2020-12/schema")
    ""
  }

}
