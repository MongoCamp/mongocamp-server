package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.client.api.AuthApi
import com.quadstingray.mongo.camp.client.model.Login
import com.quadstingray.mongo.camp.server.{ TestAdditions, TestServer }

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class BaseSuite extends munit.FunSuite {

  lazy val bearerToken: String = {
    val request       = AuthApi().login(Login("admin", TestAdditions.adminPassword))
    val loginResult   = TestAdditions.backend.send(request)
    val loginResponse = Await.result(loginResult, 1.seconds)
    val login         = loginResponse.body.getOrElse(throw new Exception("error"))
    login.authToken
  }

  override def beforeAll(): Unit = {
    if (TestServer.isServerRunning()) {
      TestAdditions.importData()
    }
  }

  override def afterAll(): Unit = {}

}
