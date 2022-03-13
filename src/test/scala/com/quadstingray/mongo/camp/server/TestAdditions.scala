package com.quadstingray.mongo.camp.server
import better.files.File
import com.quadstingray.mongo.camp.auth.{ AuthHolder, MongoAuthHolder }
import com.quadstingray.mongo.camp.converter.CirceSchema
import com.quadstingray.mongo.camp.database.MongoDatabase
import com.quadstingray.mongo.camp.model.auth.{ Grant, Role, UserInformation }
import com.sfxcode.nosql.mongo._
import io.circe.parser.decode
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
    if (!dataImported) {
      MapCollectionDao("accounts").insertMany(readJson("/accounts.json")).result()
      MapCollectionDao("users").insertMany(readJson("/users.json")).result()
      MapCollectionDao("users").insertMany(readJson("/users.json")).result()
      val geoDataDao = MapCollectionDao("geodata:locations")
      val geoJson    = readGeoDataJson()
      geoDataDao.insertMany(geoJson).result()
      geoDataDao.createIndex(Map("geodata" -> "2dsphere")).result()
      MapCollectionDao("geodata:companies").insertMany(geoJson).result()
      MapCollectionDao("test").createIndexForField("index").result()
      MapCollectionDao("admin-test").createIndexForField("index").result()

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
      dataImported = true
    }
    dataImported
  }

  def readJson(fileName: String): List[Map[String, Any]] = {
    val file        = File(getClass.getResource(fileName).getPath)
    val fileContent = file.contentAsString
    val decoded     = decode[List[Map[String, Any]]](fileContent)
    val list        = decoded.getOrElse(List())
    list
  }

  def readGeoDataJson(): List[Map[String, Any]] = {
    val list = readJson("/geodata.json").map(element => {
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
