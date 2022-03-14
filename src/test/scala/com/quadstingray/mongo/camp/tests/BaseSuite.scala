package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.client.api.AuthApi
import com.quadstingray.mongo.camp.client.model.{ Login, LoginResult }
import com.quadstingray.mongo.camp.server.{ TestAdditions, TestServer }
import io.circe
import sttp.client3.{ Identity, RequestT, Response, ResponseException }

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class BaseSuite extends munit.FunSuite {

  lazy val adminBearerToken: String    = generateBearerToken(TestAdditions.adminUser, TestAdditions.adminPassword)
  lazy val testUserBearerToken: String = generateBearerToken(TestAdditions.testUser, TestAdditions.testPassword)

  protected val collectionNameTest     = "test"
  protected val collectionNameAccounts = "accounts"
  protected val indexCollection        = "indexTestCollection"

  def executeRequest[R <: Any](
      request: RequestT[Identity, Either[ResponseException[String, circe.Error], R], Any]
  ): Response[Either[ResponseException[String, circe.Error], R]] = {
    val resultFuture   = TestAdditions.backend.send(request)
    val responseResult = Await.result(resultFuture, 1.seconds)
    responseResult
  }

  def executeRequestToResponse[R <: Any](request: RequestT[Identity, Either[ResponseException[String, circe.Error], R], Any]): R = {
    val responseResult = executeRequest(request)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    response
  }

  private def generateBearerToken(user: String, password: String): String = {
    val login: LoginResult = executeRequestToResponse(AuthApi().login(Login(user, password)))
    login.authToken
  }

  override def beforeAll(): Unit = {
    if (TestServer.isServerRunning()) {
      TestAdditions.importData()
    }
  }

  override def afterAll(): Unit = {}

}
