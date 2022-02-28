package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.client.api.DatabaseApi
import com.quadstingray.mongo.camp.server.TestAdditions

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class DatabaseSuite extends BaseSuite {

  val databaseApi: DatabaseApi = DatabaseApi()

  test("list all databases as admin") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.databaseList("", adminBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 5)
    assertEquals(response, List("admin", "config", "geodata", "local", "test"))
  }

  test("database infos as admin") {
    val resultFuture   = TestAdditions.backend.send(databaseApi.databaseInfos("", adminBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 5)
    val databaseInfoGeoDataOption = response.find(_.name.equals("geodata"))
    assertEquals(databaseInfoGeoDataOption.isDefined, true)
    assertEquals(databaseInfoGeoDataOption.get.empty, false)
    assert(databaseInfoGeoDataOption.get.sizeOnDisk > 36000)
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

}
