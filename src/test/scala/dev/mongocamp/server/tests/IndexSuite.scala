package dev.mongocamp.server.tests
import com.sfxcode.nosql.mongo.GenericObservable
import dev.mongocamp.server.client.api.IndexApi
import dev.mongocamp.server.client.model.{ IndexCreateRequest, IndexOptionsRequest }
import dev.mongocamp.server.database.MongoDatabase

class IndexSuite extends BaseSuite {

  val adminApi: IndexApi = IndexApi()

  test("create complex index as admin") {
    val request = IndexCreateRequest(
      Map("fullIndex" -> -1, "secondParamIndex" -> -1),
      IndexOptionsRequest(Some("complexIndex"), background = Some(false), unique = Some(true))
    )
    val createResult = executeRequestToResponse(adminApi.createIndex("", adminBearerToken)(indexCollection, request))
    assertEquals(createResult.name, "complexIndex")
  }

  test("get index as admin") {
    val index = executeRequestToResponse(adminApi.index("", adminBearerToken)(indexCollection, "complexIndex"))
    assertEquals(index.name, "complexIndex")
    assertEquals(index.unique, true)
  }

  test("create unique index as admin") {
    val createResult = executeRequestToResponse(adminApi.createUniqueIndex("", adminBearerToken)(indexCollection, "uniqueField"))
    assertEquals(createResult.name, "uniqueField_1")
  }

  test("create text index as admin") {
    val createResult =
      executeRequestToResponse(
        adminApi.createTextIndex("", adminBearerToken)(indexCollection, "textField", Some(IndexOptionsRequest(defaultLanguage = Some("de"))))
      )
    assertEquals(createResult.name, "textField_text")
  }

  test("create index for field as admin") {
    val createResult = executeRequestToResponse(adminApi.createIndexForField("", adminBearerToken)(indexCollection, "fieldName", Some(false)))
    assertEquals(createResult.name, "fieldName_-1")
  }

  test("create expiration index as admin") {
    val createResult = executeRequestToResponse(adminApi.createExpiringIndex("", adminBearerToken)(indexCollection, "expiration", "90s", Some(false)))
    assertEquals(createResult.name, "expiration_-1")
  }

  test("list indices as admin") {
    val listResult = executeRequestToResponse(adminApi.listIndices("", adminBearerToken)(indexCollection))
    assertEquals(listResult.size, 6)
  }

  test("create complex index as user") {
    val request = IndexCreateRequest(
      Map("fullIndex" -> -1, "secondParamIndex" -> -1),
      IndexOptionsRequest(Some("complexIndex"), background = Some(false), unique = Some(true))
    )
    val response = executeRequest(adminApi.createIndex("", testUserBearerToken)(indexCollection, request))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("get index as user") {
    val response = executeRequest(adminApi.index("", testUserBearerToken)(indexCollection, "complexIndex"))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("create unique index as user") {
    val response = executeRequest(adminApi.createUniqueIndex("", testUserBearerToken)(indexCollection, "uniqueField"))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("create text index as user") {
    val response =
      executeRequest(
        adminApi.createTextIndex("", testUserBearerToken)(indexCollection, "textField", Some(IndexOptionsRequest(defaultLanguage = Some("de"))))
      )
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("create index for field as user") {
    val response = executeRequest(adminApi.createIndexForField("", testUserBearerToken)(indexCollection, "fieldName", Some(false)))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("create expiration index as user") {
    val response = executeRequest(adminApi.createExpiringIndex("", testUserBearerToken)(indexCollection, "expiration", "90s", Some(false)))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("list indices as user") {
    val response = executeRequest(adminApi.listIndices("", testUserBearerToken)(indexCollection))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  override def afterAll(): Unit = {
    MongoDatabase.databaseProvider.dao(indexCollection).drop().result()
  }

}
