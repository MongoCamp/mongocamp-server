package com.quadstingray.mongo.camp.routes

import com.quadstingray.mongo.camp.auth.AuthHolder.isMongoDbAuthHolder
import com.quadstingray.mongo.camp.auth.{ AuthHolder, MongoAuthHolder }
import com.quadstingray.mongo.camp.database.paging.PaginationInfo
import com.quadstingray.mongo.camp.exception.{ ErrorDescription, MongoCampException }
import com.quadstingray.mongo.camp.model.JsonResult
import com.quadstingray.mongo.camp.model.auth._
import com.quadstingray.mongo.camp.routes.AuthRoutes.updateApiKey
import com.quadstingray.mongo.camp.routes.parameter.paging.{ Paging, PagingFunctions }
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
    .in(PagingFunctions.pagingParameter)
    .out(jsonBody[List[UserProfile]])
    .out(PagingFunctions.pagingHeaderOutput)
    .summary("List Users")
    .description("List all Users or filtered")
    .method(Method.GET)
    .name("listUsers")
    .serverLogic(_ => parameter => listUsers(parameter))

  def listUsers(parameter: (Option[String], Paging)): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (List[UserProfile], PaginationInfo)]] = {
    Future.successful {
      val users = AuthHolder.handler.allUsers(parameter._1, parameter._2)
      Right(users._1.map(_.toResultUser), users._2)
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
    .method(Method.PATCH)
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
    .method(Method.PATCH)
    .name("updateApiKeyForUser")
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
    Future.successful(Right(JsonResult(AuthHolder.handler.asInstanceOf[MongoAuthHolder].deleteUser(loginToUpdate))))
  }

  val updateUserUserRolesEndpoint = adminBase
    .in("users")
    .in(path[String]("userId").description("UserId to Update"))
    .in("userroles")
    .in(jsonBody[List[String]])
    .out(jsonBody[UserProfile])
    .summary("Update User Roles")
    .description("Update UserRoles of User")
    .method(Method.PATCH)
    .name("updateUserRolesForUser")
    .serverLogic(_ => loginToUpdate => updateUserRolesForUser(loginToUpdate))

  def updateUserRolesForUser(loginToUpdate: (String, List[String])): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserProfile]] = {
    Future.successful(Right(AuthHolder.handler.asInstanceOf[MongoAuthHolder].updateUsersUserRoles(loginToUpdate._1, loginToUpdate._2).toResultUser))
  }

  val listUserRolesEndpoint = adminBase
    .in("userroles")
    .in(query[Option[String]]("filter").description("filter after userId by contains"))
    .in(PagingFunctions.pagingParameter)
    .out(jsonBody[List[UserRole]])
    .out(PagingFunctions.pagingHeaderOutput)
    .summary("List UserRoles")
    .description("List all UserRolss or filtered")
    .method(Method.GET)
    .name("listUserRoles")
    .serverLogic(_ => parameter => listUserRoles(parameter))

  def listUserRoles(parameter: (Option[String], Paging)): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (List[UserRole], PaginationInfo)]] = {
    Future.successful {
      val users = AuthHolder.handler.allUserRoles(parameter._1, parameter._2)
      Right(users)
    }
  }

  val getUserRolesEndpoint = adminBase
    .in("userroles")
    .in(path[String]("userRoleName").description("UserRoleKey"))
    .out(jsonBody[UserRole])
    .summary("Get UserRole")
    .description("Get UserRole")
    .method(Method.GET)
    .name("getUserRoles")
    .serverLogic(_ => userRole => getUserRole(userRole))

  def addUserRole(userRole: UserRole): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserRole]] = {
    Future.successful {
      val role = AuthHolder.handler.asInstanceOf[MongoAuthHolder].addUserRole(userRole)
      Right(role)
    }
  }

  val addUserRolesEndpoint = adminBase
    .in("userroles")
    .in(jsonBody[UserRole])
    .out(jsonBody[UserRole])
    .summary("Add UserRole")
    .description("Add a new UserRole")
    .method(Method.PUT)
    .name("addUserRoles")
    .serverLogic(_ => userRole => addUserRole(userRole))

  def getUserRole(userRole: String): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserRole]] = {
    Future.successful {
      val role = AuthHolder.handler.findUserRole(userRole).getOrElse(throw MongoCampException("Could not find UserRole", StatusCode.NotFound))
      Right(role)
    }
  }

  val deleteUserRolesEndpoint = adminBase
    .in("userroles")
    .in(path[String]("userRoleName").description("UserRoleKey"))
    .out(jsonBody[JsonResult[Boolean]])
    .summary("Delete UserRole")
    .description("Delete UserRole")
    .method(Method.DELETE)
    .name("deleteUserRoles")
    .serverLogic(_ => userRole => deleteUserRole(userRole))

  def deleteUserRole(userRole: String): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonResult[Boolean]]] = {
    Future.successful {
      val role = AuthHolder.handler.asInstanceOf[MongoAuthHolder].deleteUserRole(userRole)
      Right(JsonResult(role))
    }
  }

  val updateUserRolesEndpoint = adminBase
    .in("userroles")
    .in(path[String]("userRoleName").description("UserRoleKey"))
    .in(jsonBody[UpdateUserRoleRequest])
    .out(jsonBody[UserRole])
    .summary("Update UserRole")
    .description("Update UserRole")
    .method(Method.PATCH)
    .name("updateUserRole")
    .serverLogic(_ => parameter => updateUserRole(parameter))

  def updateUserRole(parameter: (String, UpdateUserRoleRequest)): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserRole]] = {
    Future.successful {
      val role = AuthHolder.handler.asInstanceOf[MongoAuthHolder].updateUserRole(parameter._1, parameter._2)
      Right(role)
    }
  }

  lazy val adminEndpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = {
    val routesByHolder: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = {
      if (isMongoDbAuthHolder) {
        List(
          addUsersEndpoint,
          updateApiKeyEndpoint,
          updatePasswordEndpoint,
          updateUserUserRolesEndpoint,
          deleteUserEndpoint,
          addUserRolesEndpoint,
          updateUserRolesEndpoint,
          deleteUserRolesEndpoint
        )
      }
      else {
        List()
      }
    }
    routesByHolder ++ List(listUsersEndpoint, userEndpoint, listUserRolesEndpoint, getUserRolesEndpoint)
  }

}
