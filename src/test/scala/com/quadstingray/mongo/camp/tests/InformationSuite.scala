package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.BuildInfo
import com.quadstingray.mongo.camp.client.api.InformationApi
import com.quadstingray.mongo.camp.server.TestAdditions
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class InformationSuite extends BaseSuite {

  val informationApi = InformationApi()

  test("check version by api request") {
    val versionFuture   = TestAdditions.backend.send(InformationApi().version())
    val versionResponse = Await.result(versionFuture, 1.seconds)
    val version         = versionResponse.body.getOrElse(throw new Exception("error"))
    assertEquals(version.name, BuildInfo.name)
    assertEquals(version.version, BuildInfo.version)
    assertEquals(version.builtAt, new DateTime(BuildInfo.builtAtMillis))
  }

  test("list all databases") {
    val resultFuture   = TestAdditions.backend.send(InformationApi().databaseList("", bearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 5)
    assertEquals(response, List("admin", "config", "geodata", "local", "test"))
  }

  test("database infos") {
    val resultFuture   = TestAdditions.backend.send(InformationApi().databaseInfos("", bearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 5)
    val databaseInfoGeoDataOption = response.find(_.name.equals("geodata"))
    assertEquals(databaseInfoGeoDataOption.isDefined, true)
    assertEquals(databaseInfoGeoDataOption.get.empty, false)
    assert(databaseInfoGeoDataOption.get.sizeOnDisk > 36000)
  }

  test("list all collections") {
    val resultFuture   = TestAdditions.backend.send(InformationApi().collectionList("", bearerToken)())
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 5)
    assertEquals(response, List("accounts", "mc_request_logging", "mc_user_roles", "mc_users", "users"))
  }

  test("collection status accounts") {
    val resultFuture   = TestAdditions.backend.send(InformationApi().collectionStatus("", bearerToken)("accounts"))
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 10105.0)
    assertEquals(response.count, 100)
  }

}
