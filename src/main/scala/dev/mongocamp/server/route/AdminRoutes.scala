package dev.mongocamp.server.route

import dev.mongocamp.server.auth.AuthHolder.isMongoDbAuthHolder
import dev.mongocamp.server.auth.{ AuthHolder, MongoAuthHolder }
import dev.mongocamp.server.database.paging.PaginationInfo
import dev.mongocamp.server.event.EventSystem
import dev.mongocamp.server.event.role.{ CreateRoleEvent, DeleteRoleEvent, UpdateRoleEvent }
import dev.mongocamp.server.event.user._
import dev.mongocamp.server.exception.{ ErrorDescription, MongoCampException }
import dev.mongocamp.server.model.JsonValue
import dev.mongocamp.server.model.auth._
import dev.mongocamp.server.route.parameter.paging.{ Paging, PagingFunctions }
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
    .serverLogic(authUser => userInformation => addUser(authUser, userInformation))

  def addUser(authUser: UserInformation, userInformation: UserInformation): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserProfile]] = {
    Future.successful {
      if (userInformation.userId == null || userInformation.userId.isBlank || userInformation.userId.isEmpty) {
        throw MongoCampException("UserId could not be empty", StatusCode.PreconditionFailed)
      }
      val users = AuthHolder.handler.asInstanceOf[MongoAuthHolder].addUser(userInformation)
      EventSystem.eventStream.publish(CreateUserEvent(authUser, userInformation))
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
    .in(path[String]("userId").description("UserId to search"))
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
    .out(jsonBody[JsonValue[Boolean]])
    .summary("Update Password")
    .description("Change Password of User")
    .method(Method.PATCH)
    .name("updatePasswordForUser")
    .serverLogic(userInformation => loginToUpdate => updatePassword(userInformation, loginToUpdate))

  def updatePassword(
      userInformation: UserInformation,
      parameter: (String, PasswordUpdateRequest)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful {
      val response = AuthHolder.handler.asInstanceOf[MongoAuthHolder].updatePasswordForUser(parameter._1, parameter._2.password)
      if (response) {
        EventSystem.eventStream.publish(UpdatePasswordEvent(userInformation, parameter._1))
      }
      Right(JsonValue(response))
    }
  }

  val updateApiKeyEndpoint = adminBase
    .in("users")
    .in(path[String]("userId").description("UserId to Update"))
    .in("apikey")
    .out(jsonBody[JsonValue[String]])
    .summary("Update ApiKey")
    .description("Generate an new APIkey for the user")
    .method(Method.PATCH)
    .name("gnerateNewApiKeyForUser")
    .serverLogic(loggedInUser => loginToUpdate => updateApiKey(loggedInUser, loginToUpdate))

  def updateApiKey(
      loggedInUser: UserInformation,
      userId: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[String]]] = {
    Future.successful {
      val result = AuthHolder.handler.asInstanceOf[MongoAuthHolder].updateApiKeyUser(userId)
      EventSystem.eventStream.publish(UpdateApiKeyEvent(loggedInUser, userId))
      Right(JsonValue(result))
    }
  }

  val deleteUserEndpoint = adminBase
    .in("users")
    .in(path[String]("userId").description("UserId to Delete"))
    .out(jsonBody[JsonValue[Boolean]])
    .summary("Delete User")
    .description("Delete User")
    .method(Method.DELETE)
    .name("deleteUser")
    .serverLogic(loggedInUser => loginToUpdate => deleteUser(loggedInUser, loginToUpdate))

  def deleteUser(
      loggedInUser: UserInformation,
      loginToUpdate: String
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful({
      val response = AuthHolder.handler.asInstanceOf[MongoAuthHolder].deleteUser(loginToUpdate)
      if (response) {
        EventSystem.eventStream.publish(DeleteUserEvent(loggedInUser, loginToUpdate))
      }
      Right(JsonValue(response))
    })
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
    .serverLogic(loggedInUser => loginToUpdate => updateRolesForUser(loggedInUser, loginToUpdate))

  def updateRolesForUser(
      loggedInUser: UserInformation,
      loginToUpdate: (String, List[String])
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), UserProfile]] = {
    Future.successful(Right({
      val userInformation = AuthHolder.handler.asInstanceOf[MongoAuthHolder].updateUsersRoles(loginToUpdate._1, loginToUpdate._2)
      EventSystem.eventStream.publish(UpdateUserRoleEvent(loggedInUser, loginToUpdate._2))
      userInformation.toResultUser
    }))
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
    .in(path[String]("roleName").description("RoleKey to search"))
    .out(jsonBody[Role])
    .summary("Get Role")
    .description("Get Role by RoleKey")
    .method(Method.GET)
    .name("getRole")
    .serverLogic(_ => role => getRole(role))

  def getRole(roleKey: String): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Role]] = {
    Future.successful {
      val role = AuthHolder.handler.findRole(roleKey).getOrElse(throw MongoCampException("Could not find UserRole", StatusCode.NotFound))
      Right(role)
    }
  }

  val addRolesEndpoint = adminBase
    .in("roles")
    .in(jsonBody[Role])
    .out(jsonBody[Role])
    .summary("Add Role")
    .description("Add a new Role")
    .method(Method.PUT)
    .name("addRole")
    .serverLogic(loggedInUser => role => addRole(loggedInUser, role))

  def addRole(loggedInUser: UserInformation, role: Role): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Role]] = {
    Future.successful {
      if (role.name == null || role.name.isBlank || role.name.isEmpty) {
        throw MongoCampException("Role name could not be empty", StatusCode.PreconditionFailed)
      }
      val addResult = AuthHolder.handler.asInstanceOf[MongoAuthHolder].addRole(role)
      EventSystem.eventStream.publish(CreateRoleEvent(loggedInUser, role))
      Right(addResult)
    }
  }

  val deleteRolesEndpoint = adminBase
    .in("roles")
    .in(path[String]("roleName").description("RoleKey"))
    .out(jsonBody[JsonValue[Boolean]])
    .summary("Delete Role")
    .description("Delete Role")
    .method(Method.DELETE)
    .name("deleteRole")
    .serverLogic(loggedInUser => role => deleteRole(loggedInUser, role))

  def deleteRole(loggedInUser: UserInformation, role: String): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), JsonValue[Boolean]]] = {
    Future.successful {
      val deleted = AuthHolder.handler.asInstanceOf[MongoAuthHolder].deleteRole(role)
      if (deleted) {
        EventSystem.eventStream.publish(DeleteRoleEvent(loggedInUser, role))
      }
      Right(JsonValue(deleted))
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
    .serverLogic(loggedInUser => parameter => updateRole(loggedInUser, parameter))

  def updateRole(
      loggedInUser: UserInformation,
      parameter: (String, UpdateRoleRequest)
  ): Future[Either[(StatusCode, ErrorDescription, ErrorDescription), Role]] = {
    Future.successful {
      val role = AuthHolder.handler.asInstanceOf[MongoAuthHolder].updateRole(parameter._1, parameter._2)
      EventSystem.eventStream.publish(UpdateRoleEvent(loggedInUser, parameter._1, parameter._2))
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
