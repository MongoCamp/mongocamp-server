package dev.mongocamp.server.tests

import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.CollectionInfoApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite

// the numbers of all collections in one request
class CollectionInfoSuite extends MongoCampBaseServerSuite {

  private lazy val infoApi       = CollectionInfoApi()
  private lazy val collectionApi = CollectionApi()

  private def info(token: String, names: Seq[String] = Seq(), database: Option[String] = None) =
    executeRequestToResponse(infoApi.listCollectionInformation("", "", token, "")(names, database))

  test("an admin gets every collection with the numbers of the single request") {
    val all      = info(adminBearerToken)
    val accounts = all.find(_.name == "accounts").getOrElse(fail("accounts is missing"))
    val single   = executeRequestToResponse(collectionApi.getCollectionInformation("", "", adminBearerToken, "")("accounts"))
    assertEquals(accounts.count, single.count.toLong)
    assertEquals(accounts.nindexes, single.nindexes)
    assertEquals(accounts.available, true)
    assert(accounts.count > 0)
    assert(accounts.size > 0)
    assertEquals(all.map(_.name).toSet, executeRequestToResponse(collectionApi.listCollections("", "", adminBearerToken, "")()).toSet)
  }

  test("a user gets the collections with a read grant only") {
    val names = info(testUserBearerToken).map(_.name).toSet
    assert(names.contains("accounts"), names.toString)
    assert(names.contains("test"), names.toString)
    assertEquals(names.contains("pokemon"), false)
    assertEquals(names.contains("admin-test"), false)
    assertEquals(names, executeRequestToResponse(collectionApi.listCollections("", "", testUserBearerToken, "")()).toSet)
  }

  test("the names restrict the answer, a collection that the user may not read is not given") {
    assertEquals(info(adminBearerToken, Seq("accounts", "test")).map(_.name).toSet, Set("accounts", "test"))
    assertEquals(info(testUserBearerToken, Seq("accounts", "pokemon")).map(_.name), List("accounts"))
    assertEquals(info(adminBearerToken, Seq("does-not-exist")).size, 0)
  }

  test("an admin gets the collections of another database with their full names") {
    val geodata = info(adminBearerToken, database = Some("geodata"))
    assertEquals(geodata.map(_.name).toSet, Set("geodata:companies", "geodata:locations"))
    val locations = geodata.find(_.name == "geodata:locations").getOrElse(fail("geodata:locations is missing"))
    val single    = executeRequestToResponse(collectionApi.getCollectionInformation("", "", adminBearerToken, "")("geodata:locations"))
    assertEquals(locations.count, single.count.toLong)
    assertEquals(locations.available, true)
    assert(locations.count > 0)
  }

  test("a user gets the collections of another database with a read grant only") {
    assertEquals(info(testUserBearerToken, database = Some("geodata")).map(_.name), List("geodata:locations"))
  }

  test("the default database by name is the same as without a database") {
    val defaultName = dev.mongocamp.server.database.MongoDatabase.databaseProvider.DefaultDatabaseName
    assertEquals(info(adminBearerToken, database = Some(defaultName)).map(_.name), info(adminBearerToken).map(_.name))
  }

  test("the names restrict the answer inside the other database, plain or with the database") {
    assertEquals(info(adminBearerToken, Seq("locations"), Some("geodata")).map(_.name), List("geodata:locations"))
    assertEquals(info(adminBearerToken, Seq("geodata:locations"), Some("geodata")).map(_.name), List("geodata:locations"))
    // a full name of another database is not an entry of this database
    assertEquals(info(adminBearerToken, Seq("otherdb:locations"), Some("geodata")).size, 0)
    assertEquals(info(testUserBearerToken, Seq("locations", "companies"), Some("geodata")).map(_.name), List("geodata:locations"))
  }

  test("a database that does not exist is a 404 for an admin and an empty list for a user") {
    val admin = executeRequest(infoApi.listCollectionInformation("", "", adminBearerToken, "")(Seq(), Some("no-such-database")))
    assertEquals(admin.code.code, 404)
    assertEquals(admin.header("x-error-message"), Some("database not found"))
    assertEquals(info(testUserBearerToken, database = Some("no-such-database")).size, 0)
  }

  test("a collection that has no statistics does not fail the request") {
    // a view: the database refuses collStats for it
    val database = dev.mongocamp.server.database.MongoDatabase.databaseProvider.database()
    import dev.mongocamp.driver.mongodb._
    database.createView("accountsView", "accounts", Seq(org.mongodb.scala.bson.Document("$limit" -> 1))).result()
    try {
      val view = info(adminBearerToken).find(_.name == "accountsView").getOrElse(fail("the view is missing"))
      assertEquals(view.available, false)
      assert(
        info(adminBearerToken).exists(
          c => c.name == "accounts" && c.available
        )
      )
    }
    finally database.getCollection("accountsView").drop().result()
  }
}
