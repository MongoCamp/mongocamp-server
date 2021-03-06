package dev.mongocamp.server.route

import dev.mongocamp.server.auth.AuthHolder.isMongoDbAuthHolder
import dev.mongocamp.server.auth.{ AuthHolder, MongoAuthHolder, TokenCache }
import dev.mongocamp.server.config.ConfigHolder
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.user.{ LoginEvent, LogoutEvent, UpdateApiKeyEvent, UpdatePasswordEvent }
import dev.mongocamp.server.exception.{ ErrorCodes, ErrorDescription, MongoCampException }
import dev.mongocamp.server.model.JsonResult
import dev.mongocamp.server.model.auth._
import io.circe.generic.auto._
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.{ auth, query }

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
    .out(jsonBody[JsonResult[Boolean]])
    .summary("Check Authentication")
    .description("Check a given Login for is authenticated")
    .method(Method.GET)
    .name("isAuthenticated")
    .serverLogic(loginInformation => _ => checkAuth(loginInformation))

  def checkAuth(loginInformation: UserInformation): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonResult[Boolean]]] = {
    Future.successful {
      // If a request comes at this point User, Token etc. is valid
      Right(JsonResult(true))
    }
  }

  private val baseLogoutEndpoint = authBase
    .in("logout")
    .in(auth.bearer[Option[String]]())
    .out(jsonBody[JsonResult[Boolean]])
    .summary("Logout User")
    .description("Logout by bearer Token")

  val logoutEndpoint = baseLogoutEndpoint.method(Method.POST).name("logout").serverLogic(_ => token => logout(token))

  val logoutDeleteEndpoint = baseLogoutEndpoint.method(Method.DELETE).name("logoutByDelete").serverLogic(_ => token => logout(token))

  def logout(token: Option[String]): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonResult[Boolean]]] = {
    Future.successful {
      val result = token.forall(tokenValue => {
        TokenCache.validateToken(tokenValue).foreach(user => EventSystem.eventStream.publish(LogoutEvent(user)))
        TokenCache.invalidateToken(tokenValue)
        true
      })
      Right(JsonResult(result))
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
    .out(jsonBody[JsonResult[Boolean]])
    .summary("Update Password")
    .description("Change Password of logged in User")
    .method(Method.PATCH)
    .name("updatePassword")
    .serverLogic(loggedInUser => loginToUpdate => updatePassword(loggedInUser, loginToUpdate))

  def updatePassword(
      loggedInUser: UserInformation,
      loginToUpdate: PasswordUpdateRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonResult[Boolean]]] = {
    Future.successful {
      val result = AuthHolder.handler.asInstanceOf[MongoAuthHolder].updatePasswordForUser(loggedInUser.userId, loginToUpdate.password)
      if (result) {
        EventSystem.eventStream.publish(UpdatePasswordEvent(loggedInUser, loggedInUser.userId))
      }
      Right(JsonResult(result))
    }
  }

  val updateApiKeyEndpoint = authBase
    .in("profile")
    .in("apikey")
    .in(query[Option[String]]("userid").description("UserId to update or create the ApiKey"))
    .out(jsonBody[JsonResult[String]])
    .summary("Update ApiKey")
    .description("Generate new ApiKey of logged in User")
    .method(Method.PATCH)
    .name("generateNewApiKey")
    .serverLogic(loggedInUser => loginToUpdate => updateApiKey(loggedInUser, loginToUpdate))

  def updateApiKey(
      loggedInUser: UserInformation,
      loginToUpdate: Option[String]
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonResult[String]]] = {
    Future.successful {
      val userId = loginToUpdate.getOrElse(loggedInUser.userId)
      if (loggedInUser.userId == userId || loggedInUser.isAdmin) {
        val result = AuthHolder.handler.asInstanceOf[MongoAuthHolder].updateApiKeyUser(userId)
        EventSystem.eventStream.publish(UpdateApiKeyEvent(loggedInUser, userId))
        Right(JsonResult(result))
      }
      else {
        throw MongoCampException.unauthorizedException("user not authorized to update password for other user", ErrorCodes.unauthorizedUserForOtherUser)
      }
    }
  }

  lazy val onlyMongoEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = {
    if (isMongoDbAuthHolder) {
      List(updatePasswordEndpoint, updateApiKeyEndpoint)
    }
    else {
      List()
    }
  }

  lazy val onlyBearerEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = {
    if (ConfigHolder.authUseTypeBearer.value) {
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
