package com.quadstingray.mongo.camp.routes
import com.quadstingray.mongo.camp.auth.AuthHolder.isMongoDbAuthHolder
import com.quadstingray.mongo.camp.auth.{ AuthHolder, MongoAuthHolder }
import com.quadstingray.mongo.camp.exception.{ ErrorCodes, ErrorDescription, MongoCampException }
import com.quadstingray.mongo.camp.model.JsonResult
import com.quadstingray.mongo.camp.model.auth._
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
      Right(loginResult)
    }
  }

  private val baseLogoutEndpoint = authBase
    .in("logout")
    .in(auth.bearer[Option[String]]())
    .out(jsonBody[JsonResult[Boolean]])
    .summary("Logout User")
    .description("Logout an bearer Token")

  val logoutEndpoint = baseLogoutEndpoint.method(Method.POST).name("logout").serverLogic(_ => token => logout(token))

  val logoutDeleteEndpoint = baseLogoutEndpoint.method(Method.DELETE).name("logoutByDelete").serverLogic(_ => token => logout(token))

  def logout(token: Option[String]): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonResult[Boolean]]] = {
    Future.successful {
      val result = token.forall(tokenValue => {
        AuthHolder.tokenCache.invalidate(tokenValue)
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
      val loginResult: LoginResult = AuthHolder.handler.generateLoginResult(loginInformation)
      Right(loginResult)
    }
  }

  val profileEndpoint = authBase
    .in("profile")
    .out(jsonBody[UserProfile])
    .summary("User Profile")
    .description("Return the User Profile")
    .method(Method.GET)
    .name("userProfile")
    .serverLogic(loginInformation => _ => userProfile(loginInformation))

  def userProfile(loginInformation: UserInformation): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserProfile]] = {
    Future.successful {
      Right(loginInformation.toResultUser)
    }
  }

  val updatePasswordEndpoint = authBase
    .in("profile")
    .in("password")
    .in(jsonBody[PasswordUpdateRequest])
    .out(jsonBody[JsonResult[Boolean]])
    .summary("Update Password")
    .description("Change Password of User")
    .method(Method.PATCH)
    .name("updatePassword")
    .serverLogic(loggedInUser => loginToUpdate => updatePassword(loggedInUser, loginToUpdate))

  def updatePassword(
      loggedInUser: UserInformation,
      loginToUpdate: PasswordUpdateRequest
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonResult[Boolean]]] = {
    Future.successful {
      Right(JsonResult(AuthHolder.handler.asInstanceOf[MongoAuthHolder].updatePasswordForUser(loggedInUser.userId, loginToUpdate.password)))
    }
  }

  val updateApiKeyEndpoint = authBase
    .in("profile")
    .in("apikey")
    .in(query[Option[String]]("userid").description("UserId to update or create the ApiKey"))
    .out(jsonBody[JsonResult[String]])
    .summary("Update ApiKey")
    .description("Change ApiKey of User")
    .method(Method.PATCH)
    .name("updateApiKey")
    .serverLogic(loggedInUser => loginToUpdate => updateApiKey(loggedInUser, loginToUpdate))

  def updateApiKey(
      loggedInUser: UserInformation,
      loginToUpdate: Option[String]
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonResult[String]]] = {
    Future.successful {
      val userId = loginToUpdate.getOrElse(loggedInUser.userId)
      if (loggedInUser.userId == userId || loggedInUser.isAdmin) {
        Right(JsonResult(AuthHolder.handler.asInstanceOf[MongoAuthHolder].updateApiKeyUser(userId)))
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

  lazy val authEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] =
    List(loginEndpoint, logoutEndpoint, logoutDeleteEndpoint, refreshTokenEndpoint, profileEndpoint) ++ onlyMongoEndpoints

}
