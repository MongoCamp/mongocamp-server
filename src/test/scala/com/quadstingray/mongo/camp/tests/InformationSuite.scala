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

}
