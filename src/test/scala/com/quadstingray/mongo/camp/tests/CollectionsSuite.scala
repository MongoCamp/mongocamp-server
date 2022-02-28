package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.client.api.CollectionApi
import com.quadstingray.mongo.camp.server.TestAdditions

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class CollectionsSuite extends BaseSuite {

  val databaseApi: CollectionApi = CollectionApi()

  test("list all collections as admin") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.collectionList("", adminBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 7)
    assertEquals(response, List("accounts", "admin-test", "mc_request_logging", "mc_user_roles", "mc_users", "test", "users"))
  }

  test("collection status accounts as admin") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.collectionInformation("", adminBearerToken)("accounts"))
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 10105.0)
    assertEquals(response.count, 100)
  }

  test("list all collections as user") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.collectionList("", testUserBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 2)
    assertEquals(response, List("accounts", "test"))
  }

  test("collection status accounts as user") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.collectionInformation("", testUserBearerToken)("accounts"))
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 10105.0)
    assertEquals(response.count, 100)
  }

  test("collection status users as user") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.collectionInformation("", testUserBearerToken)("users"))
    val responseResult = Await.result(resultFuture, 1.seconds)
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for collection")
  }

}
