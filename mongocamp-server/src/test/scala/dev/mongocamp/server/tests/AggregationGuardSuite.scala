package dev.mongocamp.server.tests

import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.model
import dev.mongocamp.server.service.AggregationPipelineGuard
import dev.mongocamp.server.service.ConfigurationService
import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.model.MongoAggregateRequest
import dev.mongocamp.server.test.client.model.PipelineStage
import dev.mongocamp.server.test.MongoCampBaseServerSuite

class AggregationGuardSuite extends MongoCampBaseServerSuite {

  private lazy val collectionApi = CollectionApi()
  private val collection         = "geodata:locations"

  private def aggregate(stages: PipelineStage*) =
    executeRequest(collectionApi.aggregate("", "", adminBearerToken, "")(collection, MongoAggregateRequest(stages.toList, allowDiskUse = false)))

  private def function = Map("$function" -> Map("body" -> "function(){return 1}", "args" -> List(), "lang" -> "js"))

  private def assertRefused(response: sttp.client3.Response[?], operator: String): Unit = {
    assertEquals(response.code.code, 403)
    assertEquals(response.header("x-error-message").exists(_.contains(operator)), true, s"message does not name $operator")
  }

  test("$function inside $addFields is refused") {
    assertRefused(aggregate(PipelineStage("addFields", Map("x" -> function)), PipelineStage("limit", 1)), "$function")
  }

  test("$where inside $match is refused") {
    assertRefused(aggregate(PipelineStage("match", Map("$where" -> "this.a == 1"))), "$where")
  }

  test("$accumulator inside $group is refused") {
    val accumulator = Map("$accumulator" -> Map("init" -> "function(){return 0}", "accumulate" -> "function(){return 0}", "merge" -> "function(){return 0}", "lang" -> "js"))
    assertRefused(aggregate(PipelineStage("group", Map("_id" -> "$type", "x" -> accumulator))), "$accumulator")
  }

  test("$out and $merge are refused, also without the $ prefix") {
    assertRefused(aggregate(PipelineStage("out", "stolen")), "$out")
    assertRefused(aggregate(PipelineStage("$merge", Map("into" -> "stolen"))), "$merge")
  }

  test("a denied operator in a nested pipeline is refused") {
    val lookup = Map("from" -> "other", "as" -> "x", "pipeline" -> List(Map("$addFields" -> Map("y" -> function))))
    assertRefused(aggregate(PipelineStage("lookup", lookup)), "$function")
    val facet = Map("a" -> List(Map("$match" -> Map("$where" -> "true"))))
    assertRefused(aggregate(PipelineStage("facet", facet)), "$where")
  }

  test("a normal pipeline works") {
    val response = aggregate(PipelineStage("match", Map("type" -> Map("$exists" -> true))), PipelineStage("limit", 2))
    assertEquals(response.code.code, 200)
  }

  test("the denied operators can be configured") {
    val key = DefaultConfigurations.ConfigKeyAggregationStagesDenied
    assertEquals(ConfigurationService.updateConfig(key, List("$limit")), true)
    try {
      assertRefused(aggregate(PipelineStage("limit", 1)), "$limit")
    }
    finally {
      ConfigurationService.updateConfig(key, List("$function", "$accumulator", "$where", "$out", "$merge", "$currentOp", "$listSessions"))
    }
    assertEquals(aggregate(PipelineStage("limit", 1)).code.code, 200)
  }

  test("the guard scans lists and maps of any depth") {
    val denied = Set("$function")
    val deep   = List(model.PipelineStage("project", Map("a" -> List(1, Map("b" -> List(Map("$function" -> 1)))))))
    intercept[dev.mongocamp.server.exception.MongoCampException](AggregationPipelineGuard.check(deep, denied))
    AggregationPipelineGuard.check(List(model.PipelineStage("project", Map("a" -> List(1, 2)))), denied)
  }

  // #61: the read grant is needed for every collection that a stage reads
  private def aggregateAs(token: String, mainCollection: String, stages: PipelineStage*) =
    executeRequest(collectionApi.aggregate("", "", token, "")(mainCollection, MongoAggregateRequest(stages.toList, allowDiskUse = false)))

  private def lookup(from: Any)                 = PipelineStage("lookup", Map("from" -> from, "localField" -> "a", "foreignField" -> "b", "as" -> "x"))
  private def unionWith(coll: Any)              = PipelineStage("unionWith", coll)
  private def graphLookup(from: String)         =
    PipelineStage("graphLookup", Map("from" -> from, "startWith" -> "$a", "connectFromField" -> "a", "connectToField" -> "b", "as" -> "x"))
  private def assertCollectionRefused(response: sttp.client3.Response[?]): Unit = {
    assertEquals(response.code.code, 403)
    assertEquals(response.header("x-error-message").exists(_.contains("not allowed to read the collection")), true)
  }

  test("a lookup into a collection with a read grant works") {
    assertEquals(aggregateAs(testUserBearerToken, "accounts", lookup("users"), PipelineStage("limit", 1)).code.code, 200)
    assertEquals(aggregateAs(testUserBearerToken, "accounts", unionWith("users"), PipelineStage("limit", 1)).code.code, 200)
    assertEquals(aggregateAs(testUserBearerToken, "accounts", graphLookup("users"), PipelineStage("limit", 1)).code.code, 200)
  }

  test("a lookup into a collection without a grant is refused") {
    assertCollectionRefused(aggregateAs(testUserBearerToken, "accounts", lookup("mc_configuration")))
    assertCollectionRefused(aggregateAs(testUserBearerToken, "accounts", unionWith("mc_configuration")))
    assertCollectionRefused(aggregateAs(testUserBearerToken, "accounts", unionWith(Map("coll" -> "mc_configuration", "pipeline" -> List()))))
    assertCollectionRefused(aggregateAs(testUserBearerToken, "accounts", graphLookup("mc_configuration")))
  }

  test("a collection in a nested pipeline is checked too") {
    val nestedLookup = Map("from" -> "users", "as" -> "x", "pipeline" -> List(Map("$unionWith" -> "mc_configuration")))
    assertCollectionRefused(aggregateAs(testUserBearerToken, "accounts", PipelineStage("lookup", nestedLookup)))
    val facet = Map("a" -> List(Map("$lookup" -> Map("from" -> "mc_configuration", "as" -> "x", "pipeline" -> List()))))
    assertCollectionRefused(aggregateAs(testUserBearerToken, "accounts", PipelineStage("facet", facet)))
  }

  test("a lookup into another database is not supported") {
    val response = aggregateAs(testUserBearerToken, "accounts", lookup(Map("db" -> "other", "coll" -> "users")))
    assertEquals(response.code.code, 403)
    assertEquals(response.header("x-error-message").exists(_.contains("not supported")), true)
  }

  test("the collection of a stage is in the database of the collection of the request") {
    // geodata:locations is granted, but geodata:accounts is not the collection `accounts` of the default database
    assertCollectionRefused(aggregateAs(testUserBearerToken, "geodata:locations", lookup("accounts")))
  }

  test("an admin may read every collection in a stage") {
    assertEquals(aggregateAs(adminBearerToken, "accounts", lookup("mc_configuration"), PipelineStage("limit", 1)).code.code, 200)
  }
}
