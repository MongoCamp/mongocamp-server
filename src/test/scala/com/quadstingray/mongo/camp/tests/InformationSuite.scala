package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.BuildInfo
import com.quadstingray.mongo.camp.client.api.InformationApi
import com.quadstingray.mongo.camp.server.TestAdditions
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class InformationSuite extends BaseSuite {

  val informationApi: InformationApi = InformationApi()

  test("check version by api request") {
    val versionFuture   = TestAdditions.backend.send(informationApi.version())
    val versionResponse = Await.result(versionFuture, 1.seconds)
    val version         = versionResponse.body.getOrElse(throw new Exception("error"))
    assertEquals(version.name, BuildInfo.name)
    assertEquals(version.version, BuildInfo.version)
    assertEquals(version.builtAt, new DateTime(BuildInfo.builtAtMillis))
  }

  test("list all databases as admin") {
    val resultFuture   = TestAdditions.backend.send(informationApi.databaseList("", adminBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 5)
    assertEquals(response, List("admin", "config", "geodata", "local", "test"))
  }

  test("database infos as admin") {
    val resultFuture   = TestAdditions.backend.send(informationApi.databaseInfos("", adminBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 5)
    val databaseInfoGeoDataOption = response.find(_.name.equals("geodata"))
    assertEquals(databaseInfoGeoDataOption.isDefined, true)
    assertEquals(databaseInfoGeoDataOption.get.empty, false)
    assert(databaseInfoGeoDataOption.get.sizeOnDisk > 36000)
  }

  test("list all collections as admin") {
    val resultFuture   = TestAdditions.backend.send(informationApi.collectionList("", adminBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 7)
    assertEquals(response, List("accounts", "admin-test", "mc_request_logging", "mc_user_roles", "mc_users", "test", "users"))
  }

  test("collection status accounts as admin") {
    val resultFuture   = TestAdditions.backend.send(informationApi.collectionStatus("", adminBearerToken)("accounts"))
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 10105.0)
    assertEquals(response.count, 100)
  }

  test("list all databases as user") {
    val resultFuture   = TestAdditions.backend.send(informationApi.databaseList("", testUserBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for request")
  }

  test("database infos as user") {
    val resultFuture   = TestAdditions.backend.send(informationApi.databaseInfos("", testUserBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for request")
  }

  test("list all collections as user") {
    val resultFuture   = TestAdditions.backend.send(informationApi.collectionList("", testUserBearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 2)
    assertEquals(response, List("accounts", "test"))
  }

  test("collection status accounts as user") {
    val resultFuture   = TestAdditions.backend.send(informationApi.collectionStatus("", testUserBearerToken)("accounts"))
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 10105.0)
    assertEquals(response.count, 100)
  }

  test("collection status users as user") {
    val resultFuture   = TestAdditions.backend.send(informationApi.collectionStatus("", testUserBearerToken)("users"))
    val responseResult = Await.result(resultFuture, 1.seconds)
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for collection")
  }
}
