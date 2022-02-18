package com.quadstingray.mongo.rest.routes

import com.quadstingray.mongo.rest.auth.AuthHolder.isMongoDbAuthHolder
import com.quadstingray.mongo.rest.auth.{ AuthHolder, MongoAuthHolder }
import com.quadstingray.mongo.rest.exception.ErrorDescription
import com.quadstingray.mongo.rest.model.JsonResult
import com.quadstingray.mongo.rest.model.auth._
import com.quadstingray.mongo.rest.routes.AuthRoutes.updateApiKey
import io.circe.generic.auto._
import sttp.capabilities.WebSockets
import sttp.capabilities.akka.AkkaStreams
import sttp.model.{ Method, StatusCode }
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.{ path, query }

import scala.concurrent.Future

object AdminRoutes extends BaseRoute {
  private val adminBase = adminEndpoint.in("admin").tag("Admin")

  val listUsersEndpoint = adminBase
    .in("users")
    .in(query[Option[String]]("filter").description("filter after userId by contains"))
    .out(jsonBody[List[UserProfile]])
    .summary("List Users")
    .description("List all Users or filtered")
    .method(Method.GET)
    .name("listUsers")
    .serverLogic(_ => filter => listUsers(filter))

  def listUsers(filter: Option[String]): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[UserProfile]]] = {
    Future.successful {
      val users = AuthHolder.handler.allUsers(filter)
      Right(users.map(_.toResultUser))
    }
  }

  val addUsersEndpoint = adminBase
    .in("users")
    .in(jsonBody[UserInformation])
    .out(jsonBody[UserProfile])
    .summary("Add User")
    .description("Add a new User")
    .method(Method.PUT)
    .name("addUser")
    .serverLogic(_ => userInformation => addUser(userInformation))

  def addUser(userInformation: UserInformation): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserProfile]] = {
    Future.successful {
      val users = AuthHolder.handler.asInstanceOf[MongoAuthHolder].addUser(userInformation)
      Right(users.toResultUser)
    }
  }

  def getUser(loginToUpdate: String): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserProfile]] = {
    Future.successful {
      Right(AuthHolder.handler.asInstanceOf[MongoAuthHolder].findUser(loginToUpdate).toResultUser)
    }
  }

  val userEndpoint = adminBase
    .in("users")
    .in(path[String]("userId").description("UserId to Update"))
    .out(jsonBody[UserProfile])
    .summary("UserProfile for userId")
    .description("Get UserProfile for user")
    .method(Method.GET)
    .name("getUser")
    .serverLogic(_ => loginToUpdate => getUser(loginToUpdate))

  val updatePasswordEndpoint = adminBase
    .in("users")
    .in(path[String]("userId").description("UserId to Update"))
    .in("password")
    .in(jsonBody[PasswordUpdateRequest])
    .out(jsonBody[JsonResult[Boolean]])
    .summary("Update Password")
    .description("Change Password of User")
    .method(Method.POST)
    .name("updatePasswordForUser")
    .serverLogic(_ => loginToUpdate => updatePassword(loginToUpdate))

  def updatePassword(
      parameter: (String, PasswordUpdateRequest)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonResult[Boolean]]] = {
    Future.successful {
      Right(JsonResult(AuthHolder.handler.asInstanceOf[MongoAuthHolder].updatePasswordForUser(parameter._1, parameter._2.password)))
    }
  }

  val updateApiKeyEndpoint = adminBase
    .in("users")
    .in(path[String]("userId").description("UserId to Update"))
    .in("apikey")
    .out(jsonBody[JsonResult[String]])
    .summary("Update ApiKey")
    .description("Change Password of User")
    .method(Method.POST)
    .name("updatePasswordForUser")
    .serverLogic(loggedInUser => loginToUpdate => updateApiKey(loggedInUser, Some(loginToUpdate)))

  val deleteUserEndpoint = adminBase
    .in("users")
    .in(path[String]("userId").description("UserId to Delete"))
    .out(jsonBody[JsonResult[Boolean]])
    .summary("Delete User")
    .description("Delete User")
    .method(Method.DELETE)
    .name("deleteUser")
    .serverLogic(_ => loginToUpdate => deleteUser(loginToUpdate))

  def deleteUser(loginToUpdate: String): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonResult[Boolean]]] = {
    Future.successful {
      Right(JsonResult(AuthHolder.handler.asInstanceOf[MongoAuthHolder].deleteUser(loginToUpdate)))
    }
  }

  val updateUserUserRolesEndpoint = adminBase
    .in("users")
    .in(path[String]("userId").description("UserId to Update"))
    .in("userroles")
    .in(jsonBody[List[String]])
    .out(jsonBody[UserProfile])
    .summary("Update User Roles")
    .description("Update UserRoles of User")
    .method(Method.POST)
    .name("updateUserRolesForUser")
    .serverLogic(_ => loginToUpdate => updateUserRolesForUser(loginToUpdate))

  def updateUserRolesForUser(loginToUpdate: (String, List[String])): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserProfile]] = {
    Future.successful {
      Right(
        AuthHolder.handler.asInstanceOf[MongoAuthHolder].updateUserRoles(loginToUpdate._1, loginToUpdate._2).toResultUser
      )
    }
  }

  val listUserRolesEndpoint = adminBase
    .in("userroles")
    .in(query[Option[String]]("filter").description("filter after userId by contains"))
    .out(jsonBody[List[UserRole]])
    .summary("List UserRoles")
    .description("List all UserRolss or filtered")
    .method(Method.GET)
    .name("listUserRoles")
    .serverLogic(_ => filter => listUserRoles(filter))

  def listUserRoles(filter: Option[String]): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[UserRole]]] = {
    Future.successful {
      val users = AuthHolder.handler.allUserRoles(filter)
      Right(users)
    }
  }

  val listUserRolesGrantsEndpoint = adminBase
    .in("userroles")
    .in(path[String]("userRoleKey").description("Key of your UserRole"))
    .in("grants")
    .out(jsonBody[List[UserRoleGrant]])
    .summary("List UserRoleGrants")
    .description("List all UserRoleGrants or UserRole")
    .method(Method.GET)
    .name("listUserRoleGrants")
    .serverLogic(_ => userRoleKey => listUserRoleGrants(userRoleKey))

  def listUserRoleGrants(userRoleKey: String): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), List[UserRoleGrant]]] = {
    Future.successful {
      val userRoleGrants = AuthHolder.handler.findUserRoleGrants(userRoleKey)
      Right(userRoleGrants)
    }
  }

  lazy val adminEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = {
    val routesByHolder: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = {
      if (isMongoDbAuthHolder) {
        List(addUsersEndpoint, updateApiKeyEndpoint, updatePasswordEndpoint, updateUserUserRolesEndpoint, deleteUserEndpoint)
      }
      else {
        List()
      }
    }
    routesByHolder ++ List(listUsersEndpoint, listUserRolesEndpoint, listUserRolesGrantsEndpoint)
  }

}
