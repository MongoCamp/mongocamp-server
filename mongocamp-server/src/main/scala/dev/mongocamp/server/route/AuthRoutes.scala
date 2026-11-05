package dev.mongocamp.server.route

import dev.mongocamp.server.auth.AuthHolder
import dev.mongocamp.server.auth.MongoAuthHolder
import dev.mongocamp.server.auth.TokenCache
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.event.user.LoginEvent
import dev.mongocamp.server.event.user.LogoutEvent
import dev.mongocamp.server.event.user.UpdateApiKeyEvent
import dev.mongocamp.server.event.user.UpdatePasswordEvent
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.model.auth._
import dev.mongocamp.server.model.JsonValue
import dev.mongocamp.server.service.ConfigurationService
import io.circe.generic.auto._
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future
import sttp.capabilities.pekko.PekkoStreams
import sttp.capabilities.WebSockets
import sttp.model.Method
import sttp.model.StatusCode
import sttp.tapir.auth
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.query
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.stringToPath
object AuthRoutes extends BaseRoute {
  private val authBase = securedEndpoint.in("auth").tag("Auth")

  val loginEndpoint = baseEndpoint
    .in("auth")
    .in("login")
    .in(jsonBody[Login].description("Login Information for your Users").example(Login("myUser", "privatePassword")))
    .out(jsonBody[LoginResult])
    .summary("Login User")
    .description("Login for one user and return Login Information")
    .tag("Auth")
    .method(Method.POST)
    .name("login")
    .serverLogic(
      loginInformation => generateAuthToken(loginInformation)
    )

  def generateAuthToken(loginInformation: Login): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), LoginResult]] = {
    Future.successful {
      val user                     = AuthHolder.handler.findUser(loginInformation.userId, AuthHolder.handler.encryptPassword(loginInformation.password))
      val loginResult: LoginResult = AuthHolder.handler.generateLoginResult(user)
      EventSystem.publish(LoginEvent(user))
      Right(loginResult)
    }
  }

  val checkAuthEndpoint = authBase
    .in("authenticated")
    .out(jsonBody[JsonValue[Boolean]])
    .summary("Check Authentication")
    .description("Check a given Login for is authenticated")
    .method(Method.GET)
    .name("isAuthenticated")
    .serverLogic(
      loginInformation => _ => checkAuth(loginInformation)
    )

  def checkAuth(loginInformation: UserInformation): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful {
      // If a request comes at this point User, Token etc. is valid
      Right(JsonValue(true))
    }
  }

  private val baseLogoutEndpoint = authBase
    .in("logout")
    .in(auth.bearer[Option[String]]())
    .out(jsonBody[JsonValue[Boolean]])
    .summary("Logout User")
    .description("Logout by bearer Token")

  val logoutEndpoint = baseLogoutEndpoint
    .method(Method.POST)
    .name("logout")
    .serverLogic(
      _ => token => logout(token)
    )

  val logoutDeleteEndpoint = baseLogoutEndpoint
    .method(Method.DELETE)
    .name("logoutByDelete")
    .serverLogic(
      _ => token => logout(token)
    )

  def logout(token: Option[String]): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful {
      val result = token.forall(
        tokenValue => {
          TokenCache
            .validateToken(tokenValue)
            .foreach(
              user => EventSystem.publish(LogoutEvent(user))
            )
          TokenCache.invalidateToken(tokenValue)
          true
        }
      )
      Right(JsonValue(result))
    }
  }

  val refreshTokenEndpoint = authBase
    .in("token")
    .in("refresh")
    .in(auth.bearer[Option[String]]())
    .out(jsonBody[LoginResult])
    .summary("Refresh User")
    .description(
      "Refresh Token and return Login Information. A new token is issued on every call, the bearer token that was used for the call stays valid for a grace period (AUTH_REFRESH_GRACE_DURATION, default 60 seconds) and is invalid afterwards."
    )
    .method(Method.GET)
    .name("refreshToken")
    .serverLogic(
      loginInformation => token => refreshAuthToken(loginInformation, token)
    )

  def refreshAuthToken(
    loginInformation: UserInformation,
    token: Option[String] = None
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), LoginResult]] = {
    Future.successful {
      val loginResult: LoginResult = AuthHolder.handler.generateLoginResult(AuthHolder.handler.findUser(loginInformation.userId))
      // only a known token is superseded (a call with basic auth or an api key has no bearer token), so that unknown values cannot fill the cache
      token
        .filter(_ != loginResult.authToken)
        .filter(TokenCache.validateToken(_).isDefined)
        .foreach(
          oldToken => {
            val grace = ConfigurationService.getConfigValue[Duration](DefaultConfigurations.ConfigKeyAuthRefreshGraceDuration)
            TokenCache.supersede(oldToken, FiniteDuration(grace.toMillis, TimeUnit.MILLISECONDS))
          }
        )
      Right(loginResult)
    }
  }

  val profileEndpoint = authBase
    .in("profile")
    .out(jsonBody[UserProfile])
    .summary("User Profile")
    .description("Return the User Profile of loggedin user")
    .method(Method.GET)
    .name("userProfile")
    .serverLogic(
      loginInformation => _ => userProfile(loginInformation)
    )

  val permissionsEndpoint = authBase
    .in("permissions")
    .in(query[Option[String]]("collection").description("Resolve the rights for this collection only (also if the user has none)"))
    .out(jsonBody[List[CollectionPermission]])
    .summary("Collection permissions")
    .description("Return the resolved rights (read, write, administrate) of the loggedin user for the collections of the default database")
    .method(Method.GET)
    .name("userPermissions")
    .serverLogic(
      loginInformation => collection => userPermissions(loginInformation, collection)
    )

  def userPermissions(
    loginInformation: UserInformation,
    collection: Option[String]
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[CollectionPermission]]] = {
    Future.successful {
      val user = AuthHolder.handler.findUser(loginInformation.userId)
      Right(collection match {
        case Some(name) => List(user.permissionsFor(name))
        case None       => user.permissionsFor(MongoDatabase.databaseProvider.collectionNames())
      })
    }
  }

  def userProfile(loginInformation: UserInformation): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserProfile]] = {
    Future.successful {
      Right(AuthHolder.handler.findUser(loginInformation.userId).toResultUser)
    }
  }

  val updatePasswordEndpoint = authBase
    .in("profile")
    .in("password")
    .in(jsonBody[PasswordUpdateRequest])
    .out(jsonBody[JsonValue[Boolean]])
    .summary("Update Password")
    .description("Change Password of logged in User")
    .method(Method.PATCH)
    .name("updatePassword")
    .serverLogic(
      loggedInUser => loginToUpdate => updatePassword(loggedInUser, loginToUpdate)
    )

  def updatePassword(
    loggedInUser: UserInformation,
    loginToUpdate: PasswordUpdateRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful {
      val result = AuthHolder.handler.asInstanceOf[MongoAuthHolder].updatePasswordForUser(loggedInUser.userId, loginToUpdate.password)
      if (result) {
        EventSystem.publish(UpdatePasswordEvent(loggedInUser, loggedInUser.userId))
      }
      Right(JsonValue(result))
    }
  }

  val updateApiKeyEndpoint = authBase
    .in("profile")
    .in("apikey")
    .out(jsonBody[JsonValue[String]])
    .summary("Update ApiKey")
    .description("Generate new ApiKey of logged in User")
    .method(Method.PATCH)
    .name("generateNewApiKey")
    .serverLogic(
      loggedInUser => _ => updateApiKey(loggedInUser)
    )

  def updateApiKey(loggedInUser: UserInformation): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[String]]] = {
    Future.successful {
      val result = AuthHolder.handler.asInstanceOf[MongoAuthHolder].updateApiKeyUser(loggedInUser.userId)
      EventSystem.publish(UpdateApiKeyEvent(loggedInUser, loggedInUser.userId))
      Right(JsonValue(result))
    }
  }

  lazy val onlyMongoEndpoints: List[ServerEndpoint[PekkoStreams with WebSockets, Future]] = {
    if (AuthHolder.isMongoDbAuthHolder) {
      List(updatePasswordEndpoint, updateApiKeyEndpoint)
    }
    else {
      List()
    }
  }

  lazy val onlyBearerEndpoints: List[ServerEndpoint[PekkoStreams with WebSockets, Future]] = {
    if (ConfigurationService.getConfigValue[Boolean](DefaultConfigurations.ConfigKeyAuthBearer)) {
      List(loginEndpoint, logoutEndpoint, logoutDeleteEndpoint, refreshTokenEndpoint)
    }
    else {
      List()
    }
  }

  lazy val endpoints: List[ServerEndpoint[PekkoStreams with WebSockets, Future]] = {
    onlyBearerEndpoints ++ onlyMongoEndpoints ++ List(profileEndpoint, permissionsEndpoint, checkAuthEndpoint)
  }

}
