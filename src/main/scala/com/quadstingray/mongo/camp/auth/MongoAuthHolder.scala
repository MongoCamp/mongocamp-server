package com.quadstingray.mongo.camp.auth
import com.quadstingray.mongo.camp.auth.AuthHolder.apiKeyLength
import com.quadstingray.mongo.camp.database.MongoDatabase.{ userDao, userRolesDao }
import com.quadstingray.mongo.camp.database.paging.{ MongoPaginatedFilter, PaginationInfo }
import com.quadstingray.mongo.camp.exception.MongoCampException
import com.quadstingray.mongo.camp.exception.MongoCampException.{ apiKeyException, userOrPasswordException }
import com.quadstingray.mongo.camp.model.auth.AuthorizedCollectionRequest.allCollections
import com.quadstingray.mongo.camp.model.auth.{ CollectionGrant, UpdateUserRoleRequest, UserInformation, UserRole }
import com.quadstingray.mongo.camp.routes.parameter.paging.{ Paging, PagingFunctions }
import com.sfxcode.nosql.mongo._
import org.mongodb.scala.model.Filters
import sttp.model.StatusCode

import scala.util.Random

class MongoAuthHolder extends AuthHolder {

  private val KeyUserId    = "userId"
  private val KeyApiKey    = "apiKey"
  private val KeyPassword  = "password"
  private val KeyUserRoles = "userRoles"

  private val KeyName = "name"

  def createIndicesAndInitData(): Boolean = {
    try {

      val newPassword = Random.alphanumeric.take(10).mkString

      if (userDao.count().result() == 0) {
        val generatedUserId = "admin"
        val userRoleName    = "adminRole"
        userDao
          .insertOne(UserInformation(userId = generatedUserId, password = encryptPassword(newPassword), apiKey = None, userRoles = List(userRoleName)))
          .result()
        userDao.createUniqueIndexForField(KeyUserId).result()

        userRolesDao
          .insertOne(UserRole(userRoleName, isAdmin = true, List(CollectionGrant(allCollections, read = true, write = true, administrate = true))))
          .result()
        userRolesDao.createUniqueIndexForField(KeyName).result()

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

  override def findUserRoles(userRoles: List[String]): List[UserRole] = {
    val searchMap = Map(KeyName -> Map("$in" -> userRoles))
    userRolesDao.find(searchMap).resultList()
  }

  def addUser(userInformation: UserInformation): UserInformation = {
    if (findUserOption(userInformation.userId).isDefined) {
      throw MongoCampException("User already exists.", StatusCode.BadRequest)
    }
    val userToAdd = userInformation.copy(password = encryptPassword(userInformation.password))
    userDao.insertOne(userToAdd).result()
    findUser(userToAdd.userId, userToAdd.password)
  }

  def updateUsersUserRoles(userId: String, userRoles: List[String]): UserInformation = {
    val userInformation = findUser(userId)
    userDao.replaceOne(Map(KeyUserId -> userId), userInformation.copy(userRoles = userRoles)).result()
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
    val apiKey          = Random.alphanumeric.take(apiKeyLength).mkString
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

  override def allUserRoles(userRoleToSearch: Option[String], pagingInfo: Paging): (List[UserRole], PaginationInfo) = {
    val rowsPerPage = pagingInfo.rowsPerPage.getOrElse(PagingFunctions.DefaultRowsPerPage)
    val page        = pagingInfo.page.getOrElse(1L)
    if (userRoleToSearch.isEmpty) {
      val databasePage = MongoPaginatedFilter(userRolesDao).paginate(rowsPerPage, page)
      (databasePage.databaseObjects, databasePage.paginationInfo)
    }
    else {
      val filter       = Filters.regex(KeyName, s"(.*?)${userRoleToSearch.get}(.*?)", "i")
      val databasePage = MongoPaginatedFilter(userRolesDao, filter).paginate(rowsPerPage, page)
      (databasePage.databaseObjects, databasePage.paginationInfo)
    }
  }

  def addUserRole(userRole: UserRole): UserRole = {
    if (findUserRole(userRole.name).isDefined) {
      throw MongoCampException("UserRole already exists.", StatusCode.BadRequest)
    }
    userRolesDao.insertOne(userRole).result()
    findUserRole(userRole.name).getOrElse(throw MongoCampException("could not create UserRole", StatusCode.BadRequest))
  }

  def updateUserRole(userRoleKey: String, userRoleUpdate: UpdateUserRoleRequest): UserRole = {
    val userRole = findUserRole(userRoleKey)
    if (userRole.isEmpty) {
      throw MongoCampException("UserRole not exists.", StatusCode.NotFound)
    }
    else {
      val updated = userRole.get.copy(isAdmin = userRoleUpdate.isAdmin, collectionGrants = userRoleUpdate.collectionGrants)
      userRolesDao.replaceOne(Map(KeyName -> userRoleKey), updated).result()
      findUserRole(userRoleKey).getOrElse(throw MongoCampException("could not create UserRole", StatusCode.BadRequest))
    }
  }

  def deleteUserRole(userRole: String): Boolean = {
    val deleteResult = userRolesDao.deleteOne(Map(KeyName -> userRole)).result()
    deleteResult.wasAcknowledged() && deleteResult.getDeletedCount == 1
  }
}
