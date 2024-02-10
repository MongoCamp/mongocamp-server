package dev.mongocamp.server.database

import better.files.{ File, Resource }
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.converter.CirceSchema
import dev.mongocamp.server.model.ModelConstants
import dev.mongocamp.server.service.SystemFileService
import dev.mongocamp.server.test.CountingTestJob
import dev.mongocamp.server.test.client.api.{ AdminApi, JobsApi }
import dev.mongocamp.server.test.client.model._
import org.joda.time.DateTime
import sttp.client3.HttpClientSyncBackend

import scala.collection.mutable
import scala.util.Random

object TestAdditions extends CirceSchema {

  lazy val minPort: Int = 1000

  lazy val backend = HttpClientSyncBackend()

  lazy val adminUser: String     = "admin"
  lazy val adminPassword: String = Random.alphanumeric.take(10).mkString

  lazy val testUser: String     = "mongocamp_test"
  lazy val testPassword: String = Random.alphanumeric.take(10).mkString

  private var dataImported: Boolean = false

  val tempDir = File.newTemporaryDirectory()

  case class MapCollectionDao(collectionName: String) extends MongoDAO[Map[String, Any]](MongoDatabase.databaseProvider, collectionName)

  def importData(): Boolean = synchronized {
    MapCollectionDao("accounts").insertMany(SystemFileService.readJsonList("accounts.json")).result()
    MapCollectionDao("users").insertMany(SystemFileService.readJsonList("users.json")).result()
    MapCollectionDao("users").insertMany(SystemFileService.readJsonList("users.json")).result()
    MapCollectionDao("pokemon").insertMany(SystemFileService.readJsonList("pokedex.json")).result()
    val geoDataDao = MapCollectionDao("geodata:locations")
    val geoJson    = readGeoDataJson()
    geoDataDao.insertMany(geoJson).result()
    geoDataDao.createIndex(Map("geodata" -> "2dsphere")).result()
    MapCollectionDao("geodata:companies").insertMany(geoJson).result()
    MapCollectionDao("test").createIndexForField("index").result()
    MapCollectionDao("admin-test").createIndexForField("index").result()

    object FilesDAO extends GridFSDAO(MongoDatabase.databaseProvider, "sample-files")

    val accountFile: File = copyResourceFileToTempDir(tempDir, "accounts.json")
    val geoFile           = copyResourceFileToTempDir(tempDir, "geodata.json")
    val userFile          = copyResourceFileToTempDir(tempDir, "users.json")
    val mongocampLogoFile = copyResourceFileToTempDir(tempDir, "mongocamp.png")

    FilesDAO.uploadFile(accountFile.name, accountFile, Map("test" -> Random.alphanumeric.take(10).mkString, "fullPath" -> accountFile.toString())).result()
    FilesDAO.uploadFile(geoFile.name, geoFile, Map("test" -> Random.alphanumeric.take(10).mkString, "fullPath" -> geoFile.toString())).result()
    FilesDAO.uploadFile(userFile.name, userFile, Map("test" -> Random.alphanumeric.take(10).mkString, "fullPath" -> userFile.toString())).result()
    FilesDAO
      .uploadFile(mongocampLogoFile.name, userFile, Map("test" -> Random.alphanumeric.take(10).mkString, "fullPath" -> mongocampLogoFile.toString()))
      .result()

    dataImported
  }

  def copyResourceFileToTempDir(tempDir: File, file: String): File = {
    val tempFile    = File.newTemporaryFile(parent = Some(tempDir))
    val accountFile = tempFile.moveTo(tempFile.path.resolveSibling(file))(File.CopyOptions(overwrite = true))
    accountFile.append(Resource.asString(file).getOrElse(""))
    accountFile
  }

  def insertUsersAndRoles() = synchronized {
    if (!dataImported) {
      val userDao   = MapCollectionDao(MongoDatabase.CollectionNameUsers)
      val apiKey    = "special"
      val apiUserID = "insertApiUser"
      val apiUser   = Map("userId" -> apiUserID, "password" -> "invalidPwd", "apiKey" -> apiKey, "roles" -> List("adminRole"))
      userDao.insertOne(apiUser).result()
      val addUserRequest  = AdminApi().addUser(null, null, null, apiKey)(UserInformation(testUser, testPassword, None, List("test")))
      val addUserResponse = backend.send(addUserRequest)

      val addRoleRequest = AdminApi().addRole(null, null, null, apiKey)(
        Role(
          "test",
          isAdmin = false,
          List(
            Grant("geodata:locations", read = true, write = false, administrate = false, ModelConstants.grantTypeCollection),
            Grant("accounts", read = true, write = true, administrate = false, ModelConstants.grantTypeCollection),
            Grant("test", read = true, write = true, administrate = true, ModelConstants.grantTypeCollection),
            Grant("users", read = true, write = true, administrate = true, ModelConstants.grantTypeCollection),
            Grant("deleteTest", read = true, write = false, administrate = false, ModelConstants.grantTypeCollection)
          )
        )
      )
      val addRoleResponse = backend.send(addRoleRequest)

      val updatePasswordRequest  = AdminApi().updatePasswordForUser(null, null, null, apiKey)(adminUser, PasswordUpdateRequest(adminPassword))
      val updatePasswordResponse = backend.send(updatePasswordRequest)

      val registerJobRequest =
        JobsApi().registerJob(null, null, null, apiKey)(
          JobConfig(
            "CountingTestJob",
            classOf[CountingTestJob].getName,
            "",
            "0/5 * * ? * * *",
            ModelConstants.jobDefaultGroup,
            ModelConstants.jobDefaultPriority
          )
        )
      val registerJobResponse = backend.send(registerJobRequest)

      val deleteResult = userDao.deleteMany(Map("userId" -> apiUserID)).resultOption()

      dataImported = true
    }
  }

  def readGeoDataJson(): List[Map[String, Any]] = {
    val list = SystemFileService
      .readJsonList("geodata.json")
      .map(element => {
        val newElement = mutable.Map[String, Any]()
        newElement.put("name", element("name"))
        val geoData   = element("geodata").toString.split(", ")
        val longitude = geoData.last.toDouble
        val latitude  = geoData.head.toDouble
        newElement.put("geodata", Map("type" -> "Point", "coordinates" -> List(longitude, latitude)))
        newElement.put("type", "company")
        newElement.put("checkedAt", new DateTime(element("checkedAt")).toDate)
        newElement.toMap
      })
    list
  }

}
