package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.client.api.DatabaseApi
import com.quadstingray.mongo.camp.database.MongoDatabase
import com.quadstingray.mongo.camp.server.TestAdditions
import com.sfxcode.nosql.mongo._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class DatabaseSuite extends BaseSuite {

  val databaseApi: DatabaseApi = DatabaseApi()

  test("list all databases as admin") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.databaseList("", adminBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 6)
    assertEquals(response, List("admin", "config", "geodata", "local", "otherDB", "test"))
  }

  test("database infos as admin") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.databaseInfos("", adminBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 6)
    val databaseInfoGeoDataOption = response.find(_.name.equals("geodata"))
    assertEquals(databaseInfoGeoDataOption.isDefined, true)
    assertEquals(databaseInfoGeoDataOption.get.empty, false)
    assert(databaseInfoGeoDataOption.get.sizeOnDisk > 36000)
  }

  test("delete database infos as admin") {
    MongoDatabase.databaseProvider.dao("deleteTest:collection").createIndexForField("index_for_test").result()
    val resultFuture   = TestAdditions.backend.send(databaseApi.getDatabaseInfo("", adminBearerToken)("deleteTest"))
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.name, "deleteTest")
    val deleteResultFuture   = TestAdditions.backend.send(databaseApi.deleteDatabase("", adminBearerToken)("deleteTest"))
    val deleteResponseResult = Await.result(deleteResultFuture, 1.seconds)
    val deleteResponse       = deleteResponseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(deleteResponse.value, true)
    val result2Future   = TestAdditions.backend.send(databaseApi.getDatabaseInfo("", adminBearerToken)("deleteTest"))
    val response2Result = Await.result(result2Future, 1.seconds)
    assertEquals(response2Result.code.code, 404)
  }

  test("list all databases as user") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.databaseList("", testUserBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for request")
  }

  test("database infos as user") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.databaseInfos("", testUserBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for request")
  }

  test("delete database infos as user") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.deleteDatabase("", testUserBearerToken)("test-db"))
    val responseResult = Await.result(resultFuture, 1.seconds)
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for request")
  }
}
