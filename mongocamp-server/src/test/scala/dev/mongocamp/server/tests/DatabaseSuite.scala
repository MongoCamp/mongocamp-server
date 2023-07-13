package dev.mongocamp.server.tests
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.test.client.api.DatabaseApi

class DatabaseSuite extends BaseServerSuite {

  val databaseApi: DatabaseApi = DatabaseApi()

  test("list all databases as admin") {
    val response = executeRequestToResponse(databaseApi.listDatabases("", "", adminBearerToken, "")())
    assertEquals(response, List("admin", "config", "geodata", "local", "test"))
    assertEquals(response.size, 5)
  }

  test("database infos as admin") {
    val response = executeRequestToResponse(databaseApi.databaseInfos("", "", adminBearerToken, "")())
    assertEquals(response.size, 5)
    val databaseInfoGeoDataOption = response.find(_.name.equals("geodata"))
    assertEquals(databaseInfoGeoDataOption.isDefined, true)
    assertEquals(databaseInfoGeoDataOption.get.empty, false)
    assert(databaseInfoGeoDataOption.get.sizeOnDisk > 36000)
  }

  test("delete database infos as admin") {
    MongoDatabase.databaseProvider.dao("deleteTest:collection").createIndexForField("index_for_test").result()
    val response = executeRequestToResponse(databaseApi.getDatabaseInfo("", "", adminBearerToken, "")("deleteTest"))
    assertEquals(response.name, "deleteTest")
    val deleteResponse = executeRequestToResponse(databaseApi.deleteDatabase("", "", adminBearerToken, "")("deleteTest"))
    assertEquals(deleteResponse.value, true)
    val response2Result = executeRequest(databaseApi.getDatabaseInfo("", "", adminBearerToken, "")("deleteTest"))
    assertEquals(response2Result.code.code, 404)
  }

  test("list all collections for database test as admin") {
    val response = executeRequestToResponse(databaseApi.listCollectionsByDatabase("", "", adminBearerToken, "")("test"))
    assertEquals(
      response,
      List(
        "accounts",
        "admin-test",
        "mc_configuration",
        "mc_jobs",
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

  test("list all collections for database geodata as admin") {
    val response = executeRequestToResponse(databaseApi.listCollectionsByDatabase("", "", adminBearerToken, "")("geodata"))
    assertEquals(response.size, 2)
    assertEquals(response, List("companies", "locations"))
  }

  test("list all databases as user") {
    val responseResult = executeRequest(databaseApi.listDatabases("", "", testUserBearerToken, "")())
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for request")
  }

  test("database infos as user") {
    val responseResult = executeRequest(databaseApi.databaseInfos("", "", testUserBearerToken, "")())
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for request")
  }

  test("delete database infos as user") {
    val responseResult = executeRequest(databaseApi.deleteDatabase("", "", testUserBearerToken, "")("test-db"))
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for request")
  }

  test("list all collections for database test as user") {
    val response = executeRequestToResponse(databaseApi.listCollectionsByDatabase("", "", testUserBearerToken, "")("test"))
    assertEquals(response.size, 3)
    assertEquals(response, List("accounts", "test", "users"))
  }

  test("list all collections for database geodata as user") {
    val response = executeRequestToResponse(databaseApi.listCollectionsByDatabase("", "", testUserBearerToken, "")("geodata"))
    assertEquals(response.size, 1)
    assertEquals(response, List("locations"))
  }

}
