package dev.mongocamp.server.tests
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb.GenericObservable
import dev.mongocamp.server.client.api.AuthApi
import dev.mongocamp.server.client.model.{Login, LoginResult}
import dev.mongocamp.server.config.ConfigHolder
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.server.{TestAdditions, TestServer}
import io.circe
import sttp.client3.{Identity, RequestT, Response, ResponseException}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class BaseSuite extends munit.FunSuite with LazyLogging {

  private var _adminBearerToken: String = ""
  def clearAdminToken                   = _adminBearerToken = ""
  def adminBearerToken: String = {
    if (_adminBearerToken == "") {
      _adminBearerToken = generateBearerToken(TestAdditions.adminUser, TestAdditions.adminPassword)
    }
    _adminBearerToken
  }
  private var _testUserBearerToken: String = ""
  def clearTestUserToken                   = _testUserBearerToken = ""
  def testUserBearerToken: String = {
    if (_testUserBearerToken == "") {
      _testUserBearerToken = generateBearerToken(TestAdditions.testUser, TestAdditions.testPassword)
    }
    _testUserBearerToken
  }
  protected val collectionNameTest     = "test"
  protected val collectionNameAccounts = "accounts"
  protected val collectionNameUsers    = "users"
  protected val indexCollection        = "indexTestCollection"

  def executeRequest[R <: Any](
      request: RequestT[Identity, Either[ResponseException[String, circe.Error], R], Any]
  ): Response[Either[ResponseException[String, circe.Error], R]] = {
    val resultFuture   = TestAdditions.backend.send(request)
    val responseResult = Await.result(resultFuture, 30.seconds)
    responseResult
  }

  def executeRequestToResponse[R <: Any](request: RequestT[Identity, Either[ResponseException[String, circe.Error], R], Any]): R = {
    val responseResult = executeRequest(request)
    val response = responseResult.body.getOrElse({
      throw new Exception(responseResult.body.left.get.getMessage)
    })
    response
  }

  private def generateBearerToken(user: String, password: String): String = {
    val login: LoginResult = executeRequestToResponse(AuthApi().login(Login(user, password)))
    login.authToken
  }

  override def beforeAll(): Unit = {
    resetDatabase()
  }

  override def afterAll(): Unit = {
    resetDatabase()
  }

  private def resetDatabase(): Unit = {
    if (TestServer.isServerRunning()) {
      val databasesToIgnore = List("admin", "config", "local")
      MongoDatabase.databaseProvider.databaseNames.foreach(db => {
        if (!databasesToIgnore.contains(db)) {
          MongoDatabase.databaseProvider
            .collectionNames(db)
            .foreach(collection => {
              if (!collection.startsWith(ConfigHolder.authCollectionPrefix.value)) {
                MongoDatabase.databaseProvider.collection(s"$db:$collection").drop().result()
              }
            })
        }
      })
      TestAdditions.importData()
      TestAdditions.insertUsersAndRoles()
      clearAdminToken
      clearTestUserToken
    }
  }
}
