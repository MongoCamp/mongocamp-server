package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.client.api.DatabaseApi
import com.quadstingray.mongo.camp.database.MongoDatabase
import com.sfxcode.nosql.mongo._

class DatabaseSuite extends BaseSuite {

  val databaseApi: DatabaseApi = DatabaseApi()

  test("list all databases as admin") {
    val response = executeRequestToResponse(databaseApi.databaseList("", adminBearerToken)())
    assertEquals(response.size, 5)
    assertEquals(response, List("admin", "config", "geodata", "local", "test"))
  }

  test("database infos as admin") {
    val response = executeRequestToResponse(databaseApi.databaseInfos("", adminBearerToken)())
    assertEquals(response.size, 5)
    val databaseInfoGeoDataOption = response.find(_.name.equals("geodata"))
    assertEquals(databaseInfoGeoDataOption.isDefined, true)
    assertEquals(databaseInfoGeoDataOption.get.empty, false)
    assert(databaseInfoGeoDataOption.get.sizeOnDisk > 36000)
  }

  test("delete database infos as admin") {
    MongoDatabase.databaseProvider.dao("deleteTest:collection").createIndexForField("index_for_test").result()
    val response = executeRequestToResponse(databaseApi.getDatabaseInfo("", adminBearerToken)("deleteTest"))
    assertEquals(response.name, "deleteTest")
    val deleteResponse = executeRequestToResponse(databaseApi.deleteDatabase("", adminBearerToken)("deleteTest"))
    assertEquals(deleteResponse.value, true)
    val response2Result = executeRequest(databaseApi.getDatabaseInfo("", adminBearerToken)("deleteTest"))
    assertEquals(response2Result.code.code, 404)
  }

  test("list all databases as user") {
    val responseResult = executeRequest(databaseApi.databaseList("", testUserBearerToken)())
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for request")
  }

  test("database infos as user") {
    val responseResult = executeRequest(databaseApi.databaseInfos("", testUserBearerToken)())
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for request")
  }

  test("delete database infos as user") {
    val responseResult = executeRequest(databaseApi.deleteDatabase("", testUserBearerToken)("test-db"))
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for request")
  }
}
