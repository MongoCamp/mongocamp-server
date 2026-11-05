package dev.mongocamp.server.tests

import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.CollectionInfoApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite

// the numbers of all collections in one request
class CollectionInfoSuite extends MongoCampBaseServerSuite {

  private lazy val infoApi       = CollectionInfoApi()
  private lazy val collectionApi = CollectionApi()

  private def info(token: String, names: Seq[String] = Seq()) = executeRequestToResponse(infoApi.listCollectionInformation("", "", token, "")(names))

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
