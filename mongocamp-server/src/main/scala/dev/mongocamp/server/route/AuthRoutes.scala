package dev.mongocamp.server.route

import dev.mongocamp.server.auth.{ AuthHolder, MongoAuthHolder, TokenCache }
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.user.{ LoginEvent, LogoutEvent, UpdateApiKeyEvent, UpdatePasswordEvent }
import dev.mongocamp.server.exception.ErrorDescription
import dev.mongocamp.server.model.JsonValue
import dev.mongocamp.server.model.auth._
import dev.mongocamp.server.service.ConfigurationService
import io.circe.generic.auto._
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir.auth
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future
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
    .serverLogic(loginInformation => generateAuthToken(loginInformation))

  def generateAuthToken(loginInformation: Login): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), LoginResult]] = {
    Future.successful {
      val user                     = AuthHolder.handler.findUser(loginInformation.userId, AuthHolder.handler.encryptPassword(loginInformation.password))
      val loginResult: LoginResult = AuthHolder.handler.generateLoginResult(user)
      EventSystem.eventStream.publish(LoginEvent(user))
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
    .serverLogic(loginInformation => _ => checkAuth(loginInformation))

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

  val logoutEndpoint = baseLogoutEndpoint.method(Method.POST).name("logout").serverLogic(_ => token => logout(token))

  val logoutDeleteEndpoint = baseLogoutEndpoint.method(Method.DELETE).name("logoutByDelete").serverLogic(_ => token => logout(token))

  def logout(token: Option[String]): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful {
      val result = token.forall(tokenValue => {
        TokenCache.validateToken(tokenValue).foreach(user => EventSystem.eventStream.publish(LogoutEvent(user)))
        TokenCache.invalidateToken(tokenValue)
        true
      })
      Right(JsonValue(result))
    }
  }

  val refreshTokenEndpoint = authBase
    .in("token")
    .in("refresh")
    .out(jsonBody[LoginResult])
    .summary("Refresh User")
    .description("Refresh Token and return Login Information")
    .method(Method.GET)
    .name("refreshToken")
    .serverLogic(loginInformation => _ => refreshAuthToken(loginInformation))

  def refreshAuthToken(loginInformation: UserInformation): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), LoginResult]] = {
    Future.successful {
      val loginResult: LoginResult = AuthHolder.handler.generateLoginResult(AuthHolder.handler.findUser(loginInformation.userId))
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
    .serverLogic(loginInformation => _ => userProfile(loginInformation))

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
    .serverLogic(loggedInUser => loginToUpdate => updatePassword(loggedInUser, loginToUpdate))

  def updatePassword(
      loggedInUser: UserInformation,
      loginToUpdate: PasswordUpdateRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful {
      val result = AuthHolder.handler.asInstanceOf[MongoAuthHolder].updatePasswordForUser(loggedInUser.userId, loginToUpdate.password)
      if (result) {
        EventSystem.eventStream.publish(UpdatePasswordEvent(loggedInUser, loggedInUser.userId))
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
    .serverLogic(loggedInUser => _ => updateApiKey(loggedInUser))

  def updateApiKey(
      loggedInUser: UserInformation
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[String]]] = {
    Future.successful {
      val result = AuthHolder.handler.asInstanceOf[MongoAuthHolder].updateApiKeyUser(loggedInUser.userId)
      EventSystem.eventStream.publish(UpdateApiKeyEvent(loggedInUser, loggedInUser.userId))
      Right(JsonValue(result))
    }
  }

  lazy val onlyMongoEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = {
    if (AuthHolder.isMongoDbAuthHolder) {
      List(updatePasswordEndpoint, updateApiKeyEndpoint)
    }
    else {
      List()
    }
  }

  lazy val onlyBearerEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = {
    if (ConfigurationService.getConfigValue[Boolean](DefaultConfigurations.ConfigKeyAuthBearer)) {
      List(loginEndpoint, logoutEndpoint, logoutDeleteEndpoint, refreshTokenEndpoint)
    }
    else {
      List()
    }
  }

  lazy val authEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = {
    onlyBearerEndpoints ++ onlyMongoEndpoints ++ List(profileEndpoint, checkAuthEndpoint)
  }

}
