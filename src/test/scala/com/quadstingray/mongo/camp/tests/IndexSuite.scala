package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.client.api.IndexApi
import com.quadstingray.mongo.camp.client.model._

class IndexSuite extends BaseSuite {

  val adminApi: IndexApi = IndexApi()

  val indexCollection = "indexTestCollection"

  test("create complex index") {
    val request = IndexCreateRequest(
      Map("fullIndex" -> -1, "secondParamIndex" -> -1),
      IndexOptionsRequest(Some("complexIndex"), background = Some(false), unique = Some(true))
    )
    val createResult = executeRequestToResponse(adminApi.createIndex("", adminBearerToken)(indexCollection, request))
    assertEquals(createResult.name, "complexIndex")
  }

  test("create unique index") {
    val createResult = executeRequestToResponse(adminApi.createUniqueIndex("", adminBearerToken)(indexCollection, "uniqueField"))
    assertEquals(createResult.name, "uniqueField_1")
  }

  test("create text index") {
    val createResult =
      executeRequestToResponse(
        adminApi.createTextIndex("", adminBearerToken)(indexCollection, "textField", Some(IndexOptionsRequest(defaultLanguage = Some("de"))))
      )
    assertEquals(createResult.name, "textField_text")
  }

  test("create index for field") {
    val createResult = executeRequestToResponse(adminApi.createIndexForField("", adminBearerToken)(indexCollection, "fieldName", Some(false)))
    assertEquals(createResult.name, "fieldName_-1")
  }

  test("create expiration index") {
    val createResult = executeRequestToResponse(adminApi.createExpiringIndex("", adminBearerToken)(indexCollection, "expiration", "90s", Some(false)))
    assertEquals(createResult.name, "expiration_-1")
  }

  test("list indices") {
    val listResult = executeRequestToResponse(adminApi.listIndices("", adminBearerToken)(indexCollection))
    assertEquals(listResult.size, 6)
  }

  test("create complex index") {
    val request = IndexCreateRequest(
      Map("fullIndex" -> -1, "secondParamIndex" -> -1),
      IndexOptionsRequest(Some("complexIndex"), background = Some(false), unique = Some(true))
    )
    val response = executeRequest(adminApi.createIndex("", testUserBearerToken)(indexCollection, request))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("create unique index") {
    val response = executeRequest(adminApi.createUniqueIndex("", testUserBearerToken)(indexCollection, "uniqueField"))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("create text index") {
    val response =
      executeRequest(
        adminApi.createTextIndex("", testUserBearerToken)(indexCollection, "textField", Some(IndexOptionsRequest(defaultLanguage = Some("de"))))
      )
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("create index for field") {
    val response = executeRequest(adminApi.createIndexForField("", testUserBearerToken)(indexCollection, "fieldName", Some(false)))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("create expiration index") {
    val response = executeRequest(adminApi.createExpiringIndex("", testUserBearerToken)(indexCollection, "expiration", "90s", Some(false)))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("list indices") {
    val response = executeRequest(adminApi.listIndices("", testUserBearerToken)(indexCollection))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

}