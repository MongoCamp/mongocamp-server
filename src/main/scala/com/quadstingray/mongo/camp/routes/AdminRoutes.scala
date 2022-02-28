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

  val updateUserRolesEndpoint = adminBase
    .in("users")
    .in(path[String]("userId").description("UserId to Update"))
    .in("roles")
    .in(jsonBody[List[String]])
    .out(jsonBody[UserProfile])
    .summary("Update User Roles")
    .description("Update Roles of User")
    .method(Method.PATCH)
    .name("updateRolesForUser")
    .serverLogic(_ => loginToUpdate => updateRolesForUser(loginToUpdate))

  def updateRolesForUser(loginToUpdate: (String, List[String])): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserProfile]] = {
    Future.successful(Right(AuthHolder.handler.asInstanceOf[MongoAuthHolder].updateUsersRoles(loginToUpdate._1, loginToUpdate._2).toResultUser))
  }

  val listRolesEndpoint = adminBase
    .in("roles")
    .in(query[Option[String]]("filter").description("filter after userId by contains"))
    .in(PagingFunctions.pagingParameter)
    .out(jsonBody[List[Role]])
    .out(PagingFunctions.pagingHeaderOutput)
    .summary("List Roles")
    .description("List all Roles or filtered")
    .method(Method.GET)
    .name("listRoles")
    .serverLogic(_ => parameter => listRoles(parameter))

  def listRoles(parameter: (Option[String], Paging)): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), (List[Role], PaginationInfo)]] = {
    Future.successful {
      val users = AuthHolder.handler.allRoles(parameter._1, parameter._2)
      Right(users)
    }
  }

  val getRolesEndpoint = adminBase
    .in("roles")
    .in(path[String]("roleName").description("UserRoleKey"))
    .out(jsonBody[Role])
    .summary("Get Role")
    .description("Get Role")
    .method(Method.GET)
    .name("getRoles")
    .serverLogic(_ => role => getRole(role))

  def addRole(role: Role): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Role]] = {
    Future.successful {
      val addResult = AuthHolder.handler.asInstanceOf[MongoAuthHolder].addRole(role)
      Right(addResult)
    }
  }

  val addRolesEndpoint = adminBase
    .in("roles")
    .in(jsonBody[Role])
    .out(jsonBody[Role])
    .summary("Add Role")
    .description("Add a new Role")
    .method(Method.PUT)
    .name("addRoles")
    .serverLogic(_ => role => addRole(role))

  def getRole(roleKey: String): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Role]] = {
    Future.successful {
      val role = AuthHolder.handler.findRole(roleKey).getOrElse(throw MongoCampException("Could not find UserRole", StatusCode.NotFound))
      Right(role)
    }
  }

  val deleteRolesEndpoint = adminBase
    .in("roles")
    .in(path[String]("roleName").description("RoleKey"))
    .out(jsonBody[JsonResult[Boolean]])
    .summary("Delete Role")
    .description("Delete Role")
    .method(Method.DELETE)
    .name("deleteRoles")
    .serverLogic(_ => role => deleteRole(role))

  def deleteRole(role: String): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonResult[Boolean]]] = {
    Future.successful {
      val deleted = AuthHolder.handler.asInstanceOf[MongoAuthHolder].deleteRole(role)
      Right(JsonResult(deleted))
    }
  }

  val updateRolesEndpoint = adminBase
    .in("roles")
    .in(path[String]("roleName").description("RoleKey"))
    .in(jsonBody[UpdateRoleRequest])
    .out(jsonBody[Role])
    .summary("Update Role")
    .description("Update Role")
    .method(Method.PATCH)
    .name("updateRole")
    .serverLogic(_ => parameter => updateRole(parameter))

  def updateRole(parameter: (String, UpdateRoleRequest)): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Role]] = {
    Future.successful {
      val role = AuthHolder.handler.asInstanceOf[MongoAuthHolder].updateRole(parameter._1, parameter._2)
      Right(role)
    }
  }

  lazy val endpoints: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = {
    val routesByHolder: List[ServerEndpoint[AkkaStreams with WebSockets, Future]] = {
      if (isMongoDbAuthHolder) {
        List(
          addUsersEndpoint,
          updateApiKeyEndpoint,
          updatePasswordEndpoint,
          updateUserRolesEndpoint,
          deleteUserEndpoint,
          addRolesEndpoint,
          updateRolesEndpoint,
          deleteRolesEndpoint
        )
      }
      else {
        List()
      }
    }
    routesByHolder ++ List(listUsersEndpoint, userEndpoint, listRolesEndpoint, getRolesEndpoint)
  }

}
