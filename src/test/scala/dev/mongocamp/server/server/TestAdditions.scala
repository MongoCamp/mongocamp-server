package dev.mongocamp.server.server

import better.files.File
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.auth.{AuthHolder, MongoAuthHolder}
import dev.mongocamp.server.converter.CirceSchema
import dev.mongocamp.server.database.{JobDao, MongoDatabase}
import dev.mongocamp.server.jobs.JobPlugin
import dev.mongocamp.server.model.JobConfig
import dev.mongocamp.server.model.auth.{Grant, Role, UserInformation}
import dev.mongocamp.server.service.SystemFileService
import org.joda.time.DateTime
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.client3.SttpBackend
import sttp.client3.akkahttp.AkkaHttpBackend

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.Random

object TestAdditions extends CirceSchema {

  lazy val minPort: Int = 1000

  lazy val backend: SttpBackend[Future, AkkaStreams with capabilities.WebSockets] = AkkaHttpBackend()

  lazy val adminUser: String     = "admin"
  lazy val adminPassword: String = Random.alphanumeric.take(10).mkString

  lazy val testUser: String     = "mongocamp_test"
  lazy val testPassword: String = Random.alphanumeric.take(10).mkString

  private var dataImported: Boolean = false

  case class MapCollectionDao(collectionName: String) extends MongoDAO[Map[String, Any]](MongoDatabase.databaseProvider, collectionName)

  def importData(): Boolean = {
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
    val accountFile    = File(getClass.getResource("/accounts.json").getPath)
    val geoFile        = File(getClass.getResource("/geodata.json").getPath)
    val userFile       = File(getClass.getResource("/users.json").getPath)
    val mongoCampImage = File("docs/public/mongocamp.png")

    FilesDAO.uploadFile(accountFile.name, accountFile, Map("test" -> Random.alphanumeric.take(10).mkString, "fullPath" -> accountFile.toString())).result()
    FilesDAO.uploadFile(geoFile.name, geoFile, Map("test" -> Random.alphanumeric.take(10).mkString, "fullPath" -> geoFile.toString())).result()
    FilesDAO.uploadFile(userFile.name, userFile, Map("test" -> Random.alphanumeric.take(10).mkString, "fullPath" -> userFile.toString())).result()
    FilesDAO.uploadFile(mongoCampImage.name, userFile, Map("test" -> Random.alphanumeric.take(10).mkString, "fullPath" -> mongoCampImage.toString())).result()

    dataImported
  }

  def insertUsersAndRoles() = {
    if (!dataImported) {
      val authHolder = AuthHolder.handler.asInstanceOf[MongoAuthHolder]
      authHolder.addUser(UserInformation(testUser, testPassword, None, List("test")))
      authHolder.addRole(
        Role(
          "test",
          isAdmin = false,
          List(
            Grant("geodata:locations", read = true, write = false, administrate = false, Grant.grantTypeCollection),
            Grant("accounts", read = true, write = true, administrate = false, Grant.grantTypeCollection),
            Grant("test", read = true, write = true, administrate = true, Grant.grantTypeCollection),
            Grant("users", read = true, write = true, administrate = true, Grant.grantTypeCollection),
            Grant("deleteTest", read = true, write = false, administrate = false, Grant.grantTypeCollection)
          )
        )
      )
      authHolder.updatePasswordForUser(adminUser, adminPassword)

      JobDao().insertOne(JobConfig("CountingTestJob", classOf[CountingTestJob].getName, "", "0/5 * * ? * * *")).result()
      JobPlugin.reloadJobs()

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
