package com.quadstingray.mongo.rest.routes
import com.quadstingray.mongo.rest.auth.AuthHolder
import com.quadstingray.mongo.rest.auth.AuthHolder.expiringDuration
import com.quadstingray.mongo.rest.exception.ErrorDescription
import com.quadstingray.mongo.rest.model.JsonResult
import com.quadstingray.mongo.rest.model.auth.{ Login, LoginResult }
import io.circe.generic.auto._
import org.joda.time.DateTime
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.{ auth, Endpoint }

import scala.concurrent.Future

object AuthRoutes extends BaseRoute {

  val loginEndpoint = baseEndpoint
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
      val expirationDate = new DateTime().plusSeconds(expiringDuration.toSeconds.toInt)
      val user           = AuthHolder.handler.findUser(loginInformation.username, AuthHolder.handler.encryptPassword(loginInformation.password))
      val resultUser     = user.toResultUser
      val token          = AuthHolder.handler.encodeToken(resultUser, expirationDate)
      AuthHolder.tokenCache.put(token, user)
      Right(LoginResult(token, resultUser, expirationDate.toDate))
    }
  }

  private val baseLogoutEndpoint: Endpoint[Unit, Option[String], (StatusCode, ErrorDescription, ErrorDescription), JsonResult[Boolean], Any] = baseEndpoint
    .in("logout")
    .in(auth.bearer[Option[String]]())
    .out(jsonBody[JsonResult[Boolean]])
    .summary("Logout User")
    .description("Logout an bearer Token")
    .tag("Auth")

  val logoutEndpoint = baseLogoutEndpoint.method(Method.POST).name("logout").serverLogic(token => logout(token))

  val logoutDeleteEndpoint = baseLogoutEndpoint.method(Method.DELETE).name("logoutByDelete").serverLogic(token => logout(token))

  def logout(token: Option[String]): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonResult[Boolean]]] = {
    Future.successful {
      val result = token.forall(tokenValue => {
        AuthHolder.tokenCache.invalidate(tokenValue)
        true
      })
      Right(JsonResult(result))
    }
  }

  lazy val authEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = List(loginEndpoint, logoutEndpoint, logoutDeleteEndpoint)

}
