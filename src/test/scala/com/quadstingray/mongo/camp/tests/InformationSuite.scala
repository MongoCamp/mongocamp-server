package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.BuildInfo
import com.quadstingray.mongo.camp.client.api.InformationApi
import org.joda.time.DateTime

class InformationSuite extends BaseSuite {

  val informationApi: InformationApi = InformationApi()

  test("check version by api request") {
    val version = executeRequestToResponse(informationApi.version())
    assertEquals(version.name, BuildInfo.name)
    assertEquals(version.version, BuildInfo.version)
    assertEquals(version.builtAt, new DateTime(BuildInfo.builtAtMillis))
  }

}
