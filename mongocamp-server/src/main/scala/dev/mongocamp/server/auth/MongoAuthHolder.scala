package dev.mongocamp.server.auth

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.config.DefaultConfigurations
import dev.mongocamp.server.database.MongoDaoHolder.{rolesDao, userDao}
import dev.mongocamp.server.database.paging.{MongoPaginatedFilter, PaginationInfo}
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.exception.MongoCampException.{apiKeyException, userOrPasswordException}
import dev.mongocamp.server.model.ModelConstants
import dev.mongocamp.server.model.auth.AuthorizedCollectionRequest.all
import dev.mongocamp.server.model.auth.{Grant, Role, UpdateRoleRequest, UserInformation}
import dev.mongocamp.server.route.parameter.paging.{Paging, PagingFunctions}
import dev.mongocamp.server.service.ConfigurationService
import org.mongodb.scala.model.Filters
import sttp.model.StatusCode

import scala.util.Random

class MongoAuthHolder extends AuthHolder {

  private val KeyUserId   = "userId"
  private val KeyApiKey   = "apiKey"
  private val KeyPassword = "password"
  private val KeyRoles    = "roles"

  private val KeyName = "name"

  def createIndicesAndInitData(): Boolean = {
    try {

      val newPassword = Random.alphanumeric.take(10).mkString

      if (userDao.count().result() == 0) {
        val generatedUserId = "admin"
        val roleName        = "adminRole"
        userDao
          .insertOne(UserInformation(userId = generatedUserId, password = encryptPassword(newPassword), apiKey = None, roles = List(roleName)))
          .result()

        userDao.createUniqueIndexForField(KeyUserId).result()

        rolesDao
          .insertOne(
            Role(
              roleName,
              isAdmin = true,
              List(
                Grant(all, read = true, write = true, administrate = true, ModelConstants.grantTypeCollection),
                Grant(all, read = true, write = true, administrate = true, ModelConstants.grantTypeBucket)
              )
            )
          )
          .result()
        rolesDao.createUniqueIndexForField(KeyName).result()

        println("****************************************")
        println(s"* user: $generatedUserId")
        println(s"* Password: $newPassword")
        println("****************************************")
      }
      true
    }
    catch {
      case _: Exception => false
    }
  }

  override def findUserOption(userId: String) = {
    userDao.find(KeyUserId, userId).resultOption()
  }
  override def findUser(userId: String, password: String): UserInformation = {
    userDao.find(Map(KeyUserId -> userId, KeyPassword -> password)).resultOption().getOrElse(throw userOrPasswordException)
  }

  override def findRoles(roles: List[String]): List[Role] = {
    val searchMap = Map(KeyName -> Map("$in" -> roles))
    rolesDao.find(searchMap).resultList()
  }

  def addUser(userInformation: UserInformation): UserInformation = {
    if (findUserOption(userInformation.userId).isDefined) {
      throw MongoCampException("User already exists.", StatusCode.BadRequest)
    }
    val userToAdd = userInformation.copy(password = encryptPassword(userInformation.password))
    userDao.insertOne(userToAdd).result()
    findUser(userToAdd.userId, userToAdd.password)
  }

  def updateUsersRoles(userId: String, roles: List[String]): UserInformation = {
    val userInformation = findUser(userId)
    userDao.replaceOne(Map(KeyUserId -> userId), userInformation.copy(roles = roles)).result()
    findUser(userId)
  }

  def deleteUser(userId: String): Boolean = {
    val insertResult = userDao.deleteOne(Map(KeyUserId -> userId)).result()
    insertResult.wasAcknowledged() && insertResult.getDeletedCount == 1
  }

  def updatePasswordForUser(userId: String, newPassword: String): Boolean = {
    val userInformation = findUser(userId)
    val updateResult    = userDao.replaceOne(Map(KeyUserId -> userId), userInformation.copy(password = encryptPassword(newPassword))).result()
    updateResult.wasAcknowledged() && updateResult.getModifiedCount == 1
  }

  def updateApiKeyUser(userId: String): String = {
    val userInformation = findUser(userId)
    val apiKey          = Random.alphanumeric.take(ConfigurationService.getConfigValue[Long](DefaultConfigurations.ConfigKeyAuthApiKeyLength).toInt).mkString
    val updateResult    = userDao.replaceOne(Map(KeyUserId -> userId), userInformation.copy(apiKey = Some(apiKey))).result()
    if (updateResult.wasAcknowledged() && updateResult.getModifiedCount == 1) {
      apiKey
    }
    else {
      throw MongoCampException("could not update apikey for user", StatusCode.BadRequest)
    }
  }

  override def findUserByApiKeyOption(apiKey: String): Option[UserInformation] = {
    if (apiKey == null) {
      throw apiKeyException
    }
    else {
      val searchMap = Map(KeyApiKey -> apiKey)
      userDao.find(searchMap).resultOption()
    }
  }

  override def allUsers(userToSearch: Option[String], pagingInfo: Paging): (List[UserInformation], PaginationInfo) = {
    val rowsPerPage = pagingInfo.rowsPerPage.getOrElse(PagingFunctions.DefaultRowsPerPage)
    val page        = pagingInfo.page.getOrElse(1L)
    if (userToSearch.isEmpty) {
      val databasePage = MongoPaginatedFilter(userDao).paginate(rowsPerPage, page)
      (databasePage.databaseObjects, databasePage.paginationInfo)
    }
    else {
      val filter       = Filters.regex(KeyUserId, s"(.*?)${userToSearch.get}(.*?)", "i")
      val databasePage = MongoPaginatedFilter(userDao, filter).paginate(rowsPerPage, page)
      (databasePage.databaseObjects, databasePage.paginationInfo)
    }
  }

  override def allRoles(roleToSearch: Option[String], pagingInfo: Paging): (List[Role], PaginationInfo) = {
    val rowsPerPage = pagingInfo.rowsPerPage.getOrElse(PagingFunctions.DefaultRowsPerPage)
    val page        = pagingInfo.page.getOrElse(1L)
    if (roleToSearch.isEmpty) {
      val databasePage = MongoPaginatedFilter(rolesDao).paginate(rowsPerPage, page)
      (databasePage.databaseObjects, databasePage.paginationInfo)
    }
    else {
      val filter       = Filters.regex(KeyName, s"(.*?)${roleToSearch.get}(.*?)", "i")
      val databasePage = MongoPaginatedFilter(rolesDao, filter).paginate(rowsPerPage, page)
      (databasePage.databaseObjects, databasePage.paginationInfo)
    }
  }

  def addRole(role: Role): Role = {
    if (findRole(role.name).isDefined) {
      throw MongoCampException("UserRole already exists.", StatusCode.BadRequest)
    }
    rolesDao.insertOne(role).result()
    findRole(role.name).getOrElse(throw MongoCampException("could not create UserRole", StatusCode.BadRequest))
  }

  def updateRole(roleKey: String, roleUpdate: UpdateRoleRequest): Role = {
    val role = findRole(roleKey)
    if (role.isEmpty) {
      throw MongoCampException("UserRole not exists.", StatusCode.NotFound)
    }
    else {
      val updated = role.get.copy(isAdmin = roleUpdate.isAdmin, collectionGrants = roleUpdate.collectionGrants)
      rolesDao.replaceOne(Map(KeyName -> roleKey), updated).result()
      findRole(roleKey).getOrElse(throw MongoCampException("could not create UserRole", StatusCode.BadRequest))
    }
  }

  def deleteRole(role: String): Boolean = {
    val deleteResult = rolesDao.deleteOne(Map(KeyName -> role)).result()
    deleteResult.wasAcknowledged() && deleteResult.getDeletedCount == 1
  }
}
