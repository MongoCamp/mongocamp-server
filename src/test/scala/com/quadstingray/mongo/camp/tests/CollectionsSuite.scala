package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.client.api.CollectionApi
import com.quadstingray.mongo.camp.database.MongoDatabase
import com.quadstingray.mongo.camp.server.TestAdditions
import com.sfxcode.nosql.mongo._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class CollectionsSuite extends BaseSuite {

  val databaseApi: CollectionApi = CollectionApi()

  test("list all collections as admin") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.collectionList("", adminBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 7)
    assertEquals(response, List("accounts", "admin-test", "mc_request_logging", "mc_roles", "mc_users", "test", "users"))
  }

  test("collection status accounts as admin") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.getCollectionInformation("", adminBearerToken)("accounts"))
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 10105.0)
    assertEquals(response.count, 100)
  }

  test("delete collection infos as admin") {
    MongoDatabase.databaseProvider.dao("deleteTest").createIndexForField("index_for_test").result()
    val resultFuture   = TestAdditions.backend.send(databaseApi.getCollectionInformation("", adminBearerToken)("deleteTest"))
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.ns, "test.deleteTest")
    val deleteResultFuture   = TestAdditions.backend.send(databaseApi.deleteCollection("", adminBearerToken)("deleteTest"))
    val deleteResponseResult = Await.result(deleteResultFuture, 1.seconds)
    val deleteResponse       = deleteResponseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(deleteResponse.value, true)
  }

  test("clear collection infos as admin") {
    val collection = MongoDatabase.databaseProvider.dao("otherDB:collectionName")
    collection.insertOne(Map("key" -> "value")).result()
    assertEquals(collection.count().result(), 1L)
    val deleteResultFuture   = TestAdditions.backend.send(databaseApi.clearCollection("", adminBearerToken)("otherDB:collectionName"))
    val deleteResponseResult = Await.result(deleteResultFuture, 1.seconds)
    deleteResponseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(collection.count().result(), 0L)
  }

  test("list all collections as user") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.collectionList("", testUserBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 2)
    assertEquals(response, List("accounts", "test"))
  }

  test("collection status accounts as user") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.getCollectionInformation("", testUserBearerToken)("accounts"))
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 10105.0)
    assertEquals(response.count, 100)
  }

  test("collection status users as user") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.getCollectionInformation("", testUserBearerToken)("users"))
    val responseResult = Await.result(resultFuture, 1.seconds)
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for collection")
  }

  test("delete collection infos as user") {
    MongoDatabase.databaseProvider.dao("deleteTest").createIndexForField("index_for_test").result()
    val resultFuture   = TestAdditions.backend.send(databaseApi.getCollectionInformation("", testUserBearerToken)("deleteTest"))
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.ns, "test.deleteTest")
    val deleteResultFuture   = TestAdditions.backend.send(databaseApi.deleteCollection("", testUserBearerToken)("deleteTest"))
    val deleteResponseResult = Await.result(deleteResultFuture, 1.seconds)
    assertEquals(deleteResponseResult.code.code, 401)
    assertEquals(deleteResponseResult.header("x-error-message").isDefined, true)
    assertEquals(deleteResponseResult.header("x-error-message").get, "user not authorized for collection")
  }

}
