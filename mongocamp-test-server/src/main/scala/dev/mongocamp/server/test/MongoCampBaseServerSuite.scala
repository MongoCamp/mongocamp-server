package dev.mongocamp.server.test

import dev.mongocamp.driver.mongodb.GenericObservable
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.database.{MongoDatabase, TestAdditions}
import dev.mongocamp.server.service.ConfigurationRead
import dev.mongocamp.server.test.client.api.AuthApi
import dev.mongocamp.server.test.client.model.{Login, LoginResult}
import io.circe
import sttp.client3.{Identity, RequestT, Response, ResponseException}

trait MongoCampBaseServerSuite extends munit.FunSuite {

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
    resultFuture
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
              val configRead = new ConfigurationRead {
                override protected def publishConfigUpdateEvent(key: String, newValue: Any, oldValue: Any, callingMethod: String): Unit = {}
              }
              if (!collection.startsWith(configRead.getConfigValue[String](DefaultConfigurations.ConfigKeyAuthPrefix))) {
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
