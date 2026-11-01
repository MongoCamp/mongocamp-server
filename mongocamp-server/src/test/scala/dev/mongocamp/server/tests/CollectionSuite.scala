package dev.mongocamp.server.tests

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.DatabaseApi
import dev.mongocamp.server.test.client.model.MongoAggregateRequest
import dev.mongocamp.server.test.client.model.PipelineStage
import dev.mongocamp.server.test.MongoCampBaseServerSuite

class CollectionSuite extends MongoCampBaseServerSuite {

  lazy val collectionApi: CollectionApi = CollectionApi()
  lazy val databaseApi: DatabaseApi     = DatabaseApi()

  test("list all collections as admin") {
    val response = executeRequestToResponse(collectionApi.listCollections("", "", adminBearerToken, "")())
    assertEquals(response.size, 17)
    assertEquals(
      response,
      List(
        "accounts",
        "admin-test",
        "mc_configuration",
        "mc_jobs_CRON_TRIGGERS",
        "mc_jobs_FIRED_TRIGGERS",
        "mc_jobs_JOB_DETAILS",
        "mc_jobs_LOCKS",
        "mc_jobs_SCHEDULER_STATE",
        "mc_jobs_TRIGGERS",
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
  }

  test("collection status accounts as admin") {
    val response = executeRequestToResponse(collectionApi.getCollectionInformation("", "", adminBearerToken, "")("accounts"))
    assertEquals(response.size, 10105.0)
    assertEquals(response.count, 100)
  }

  test("delete collection infos as admin") {
    MongoDatabase.databaseProvider.dao("deleteTest").createIndexForField("index_for_test").result()
    val response = executeRequestToResponse(collectionApi.getCollectionInformation("", "", adminBearerToken, "")("deleteTest"))
    assertEquals(response.ns, "test.deleteTest")
    val deleteResponse = executeRequestToResponse(collectionApi.deleteCollection("", "", adminBearerToken, "")("deleteTest"))
    assertEquals(deleteResponse.value, true)
  }

  test("clear collection infos as admin") {
    val collection = MongoDatabase.databaseProvider.dao("otherDB:collectionName")
    collection.insertOne(Map("key" -> "value")).result()
    assertEquals(collection.count().result(), 1L)
    val deleteResponseResult = executeRequestToResponse(collectionApi.clearCollection("", "", adminBearerToken, "")("otherDB:collectionName"))
    assertEquals(deleteResponseResult.value, true)
    assertEquals(collection.count().result(), 0L)
    val databaseDelete = executeRequestToResponse(databaseApi.deleteDatabase("", "", adminBearerToken, "")("otherDB"))
    assertEquals(databaseDelete.value, true)
  }

  test("distinct on collection as admin") {
    val distinct = executeRequestToResponse(collectionApi.distinct("", "", adminBearerToken, "")("users", "numberrange"))
    assertEquals(distinct.size, 11)
    assertEquals(
      distinct.sortBy(
        f => f.toString.toLong
      ),
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    )
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
    val aggregateRequest = MongoAggregateRequest(List(PipelineStage("geoNear", geoNearMap)), allowDiskUse = true)
    val aggreationResult = executeRequestToResponse(collectionApi.aggregate("", "", adminBearerToken, "")("geodata:locations", aggregateRequest))
    assertEquals(aggreationResult.size, 1)
    val headResponse = aggreationResult.head
    assertEquals(headResponse("name").toString, "Mollis Dui Associates")
    assertEquals(headResponse("type").toString, "company")
  }

  test("aggregation with an empty result answers an empty list") {
    val noHit = PipelineStage("match", Map("name" -> "no-such-name"))
    val matchOnly = executeRequestToResponse(
      collectionApi.aggregate("", "", adminBearerToken, "")("geodata:locations", MongoAggregateRequest(List(noHit), allowDiskUse = false))
    )
    assertEquals(matchOnly.size, 0)
    val matchAndCount = executeRequestToResponse(
      collectionApi
        .aggregate("", "", adminBearerToken, "")("geodata:locations", MongoAggregateRequest(List(noHit, PipelineStage("count", "total")), allowDiskUse = false))
    )
    assertEquals(matchAndCount.size, 0)
  }

  test("database errors do not contain the address of the database server") {
    val invalidLimit = MongoAggregateRequest(List(PipelineStage("limit", "abc")), allowDiskUse = false)
    val response     = executeRequest(collectionApi.aggregate("", "", adminBearerToken, "")("geodata:locations", invalidLimit))
    assertEquals(response.code.code, 500)
    val message = response.body.left.map(_.getMessage).left.getOrElse("")
    assertEquals(message.isBlank, false, "the error response has no message")
    assertEquals(message.contains("on server"), false, s"the error response contains the server address: $message")
    assertEquals(message.contains("The full response is"), false, s"the error response contains the full server response: $message")
  }

  test("fields on collection as admin") {
    val distinct = executeRequestToResponse(collectionApi.getCollectionFields("", "", adminBearerToken, "")("geodata:locations"))
    assertEquals(distinct.size, 5)
    assertEquals(distinct.sorted, List("_id", "checkedAt", "geodata", "name", "type"))
  }

  test("list all collections as user") {
    val response = executeRequestToResponse(collectionApi.listCollections("", "", testUserBearerToken, "")())
    assertEquals(response.size, 3)
    assertEquals(response, List("accounts", "test", "users"))
  }

  test("collection status accounts as user") {
    val response = executeRequestToResponse(collectionApi.getCollectionInformation("", "", testUserBearerToken, "")("accounts"))
    assertEquals(response.size, 10105.0)
    assertEquals(response.count, 100)
  }

  test("collection status companies as user") {
    val responseResult = executeRequest(collectionApi.getCollectionInformation("", "", testUserBearerToken, "")("companies"))
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for collection")
  }

  test("distinct on collection as user") {
    val distinct = executeRequestToResponse(collectionApi.distinct("", "", testUserBearerToken, "")("users", "numberrange"))
    assertEquals(distinct.size, 11)
    assertEquals(
      distinct.sortBy(
        f => f.toString.toLong
      ),
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    )
  }

  // #75: dropping answers 200 whether or not the collection exists
  test("drop a collection that does not exist") {
    val response = executeRequestToResponse(collectionApi.deleteCollection("", "", adminBearerToken, "")("collectionThatDoesNotExist"))
    assertEquals(response.value, true)
  }

  test("delete collection infos as user") {
    MongoDatabase.databaseProvider.dao("deleteTest").createIndexForField("index_for_test").result()
    val response = executeRequestToResponse(collectionApi.getCollectionInformation("", "", testUserBearerToken, "")("deleteTest"))
    assertEquals(response.ns, "test.deleteTest")
    val deleteResponseResult = executeRequest(collectionApi.deleteCollection("", "", testUserBearerToken, "")("deleteTest"))
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
    val aggregateRequest = MongoAggregateRequest(List(PipelineStage("geoNear", geoNearMap)), allowDiskUse = true)
    val aggreationResult = executeRequestToResponse(collectionApi.aggregate("", "", testUserBearerToken, "")("geodata:locations", aggregateRequest))
    assertEquals(aggreationResult.size, 1)
    val headResponse = aggreationResult.head
    assertEquals(headResponse("name").toString, "Mollis Dui Associates")
    assertEquals(headResponse("type").toString, "company")
  }

  test("check schema of `users` collection") {
    val schemaAnalysis = executeRequestToResponse(collectionApi.getSchemaAnalysis("", "", adminBearerToken, "")("users"))
    assertEquals(schemaAnalysis.count, 1000L)
    assertEquals(schemaAnalysis.sample, 1000L)
    assertEquals(schemaAnalysis.percentageOfAnalysed, 1.0)
    val emailField = schemaAnalysis.fields.get.filter(
      f => f.name.equalsIgnoreCase("email")
    )
    assertEquals(emailField.nonEmpty, true)
    assertEquals(emailField.head.percentageOfParent, 1.0)
  }

  test("schema analysis only contains the fields of the requested collection") {
    def analysis(collection: String)         = executeRequestToResponse(collectionApi.getSchemaAnalysis("", "", adminBearerToken, "")(collection))
    def fieldNames(collection: String)       = analysis(collection).fields.getOrElse(Seq()).map(_.name).toSet
    def collectionFields(collection: String) = executeRequestToResponse(collectionApi.getCollectionFields("", "", adminBearerToken, "")(collection)).toSet

    val usersFirst = fieldNames("users")
    val pokemon    = fieldNames("pokemon")
    val usersAgain = fieldNames("users")
    assertEquals(usersFirst, collectionFields("users"))
    assertEquals(pokemon, collectionFields("pokemon"))
    // the result of a repeated request is identical, it does not grow with every request
    assertEquals(usersAgain, usersFirst)
    assertEquals(analysis("users").fields.getOrElse(Seq()).size, analysis("users").fields.getOrElse(Seq()).size)
    assertEquals(pokemon.contains("email"), false, "fields of `users` are part of the analysis of `pokemon`")
  }

  test("json schema only contains the definitions of the requested collection") {
    def jsonSchema(collection: String) = executeRequestToResponse(collectionApi.getJsonSchema("", "", adminBearerToken, "")(collection))
    val users                          = jsonSchema("users")
    val pokemon                        = jsonSchema("pokemon")
    assertEquals(pokemon.definitions.contains(users.$ref), false, s"definitions of `users` (${users.$ref}) are part of the schema of `pokemon`")
    assertEquals(users.definitions.contains(pokemon.$ref), false, s"definitions of `pokemon` (${pokemon.$ref}) are part of the schema of `users`")
    assertEquals(jsonSchema("users").definitions.keySet, users.definitions.keySet)
  }

  test("check schema of `pokemon` collection") {
    val jsonSchema = executeRequestToResponse(collectionApi.getJsonSchema("", "", adminBearerToken, "")("pokemon"))
    assertEquals(jsonSchema.$schema, "https://json-schema.org/draft/2020-12/schema")
    ""
  }

}
