package com.quadstingray.mongo.camp.auth
import com.quadstingray.mongo.camp.auth.AuthHolder.apiKeyLength
import com.quadstingray.mongo.camp.database.MongoDatabase.{ userDao, userRolesDao }
import com.quadstingray.mongo.camp.exception.MongoCampException
import com.quadstingray.mongo.camp.exception.MongoCampException.{ apiKeyException, userNotFoundException, userOrPasswordException }
import com.quadstingray.mongo.camp.model.auth.AuthorizedCollectionRequest.allCollections
import com.quadstingray.mongo.camp.model.auth.{ CollectionGrant, UserInformation, UserRole }
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

  override def findUser(userId: String): UserInformation = {
    userDao.find(KeyUserId, userId).resultOption().getOrElse(throw userNotFoundException)
  }

  override def findUser(userId: String, password: String): UserInformation = {
    val searchMap = Map(KeyUserId -> userId, KeyPassword -> password)
    userDao.find(searchMap).resultOption().getOrElse(throw userOrPasswordException)
  }

  override def findUserRoles(userRoles: List[String]): List[UserRole] = {
    val searchMap = Map(KeyName -> Map("$in" -> userRoles))
    userRolesDao.find(searchMap).resultList()
  }

  def addUser(userInformation: UserInformation): UserInformation = {
    val userToAdd = userInformation.copy(password = encryptPassword(userInformation.password))
    userDao.insertOne(userToAdd).result()
    findUser(userToAdd.userId, userToAdd.password)
  }

  def updateUserRoles(userId: String, userRoles: List[String]): UserInformation = {
    val userInformation = findUser(userId)
    userDao.replaceOne(Map(KeyUserId -> userId), userInformation.copy(userRoles = userRoles)).result()
    userDao.find(KeyUserId, userId).resultOption().getOrElse(throw userNotFoundException)
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

  override def findUserByApiKey(apiKey: String): UserInformation = {
    if (apiKey == null) {
      throw apiKeyException
    }
    else {
      val searchMap = Map(KeyApiKey -> apiKey)
      userDao.find(searchMap).resultOption().getOrElse(throw apiKeyException)
    }
  }

  override def allUsers(userToSearch: Option[String]): List[UserInformation] = {
    if (userToSearch.isEmpty) {
      userDao.find().resultList()
    }
    else {
      val filter = Filters.regex(KeyUserId, s"(.*?)${userToSearch.get}(.*?)", "i")
      userDao.find(filter).resultList()
    }
  }

  override def allUserRoles(userRoleToSearch: Option[String]): List[UserRole] = {
    if (userRoleToSearch.isEmpty) {
      userRolesDao.find().resultList()
    }
    else {
      val filter = Filters.regex(KeyName, s"(.*?)${userRoleToSearch.get}(.*?)", "i")
      userRolesDao.find(filter).resultList()
    }
  }
}
