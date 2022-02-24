package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.server.{ TestAdditions, TestServer }

class BaseSuite extends munit.FunSuite {

  override def beforeAll(): Unit = {
    if (TestServer.isServerRunning()) {
      TestAdditions.importData()
    }
  }

  override def afterAll(): Unit = {}

}
