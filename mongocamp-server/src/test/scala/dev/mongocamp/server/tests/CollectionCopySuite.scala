package dev.mongocamp.server.tests

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.test.client.api.CollectionRenameApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import io.circe.parser.parse
import io.circe.Json
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.IndexOptions

// copy a collection in the database, with a filter and with the indices
class CollectionCopySuite extends MongoCampBaseServerSuite {

  private lazy val copyApi = CollectionRenameApi()

  private def provider = MongoDatabase.databaseProvider

  private def copy(collection: String, body: String, token: String = adminBearerToken) =
    executeRequest(copyApi.copyCollection("", "", token, "")(collection, parse(body).getOrElse(Json.Null)))

  private def exists(name: String) = provider.collectionNames().contains(name)

  private def count(name: String) = provider.dao(name).count().result()

  private def indexNames(name: String) = provider.dao(name).indexList().map(_.name).toSet

  private def cleanup(names: String*): Unit = names.foreach(
    name => if (exists(name)) provider.dao(name).drop().result()
  )

  private def temporaryCollections = provider.collectionNames().filter(_.startsWith("mc_copy_"))

  test("prepare") {
    val dao = provider.dao("copySource")
    dao
      .insertMany(
        (1 to 12).map(
          i => Document("i" -> i, "type" -> (if (i <= 5) "a" else "b"), "title" -> s"t$i", "body" -> "b")
        )
      )
      .result()
    dao.createIndex(Document("type" -> 1, "i" -> -1), IndexOptions().name("compound")).result()
    dao.createIndex(Document("i" -> 1), IndexOptions().name("uniqueI").unique(true).partialFilterExpression(Document("i" -> Document("$gt" -> 0)))).result()
    dao.createIndex(Document("title" -> "text", "body" -> "text"), IndexOptions().name("texts").defaultLanguage("german")).result()
  }

  test("a collection is copied with its indices, the source stays as it is") {
    val response = copy("copySource", """{"target": "copyAll"}""")
    assertEquals(response.code.code, 200, response.body.fold(_.getMessage, _ => ""))
    val body = response.body.getOrElse(throw new Exception("no body"))
    assertEquals(body.hcursor.get[String]("name"), Right("copyAll"))
    assertEquals(body.hcursor.get[Long]("count"), Right(12L))
    assertEquals(body.hcursor.get[Int]("indexes"), Right(3))
    assertEquals(count("copyAll"), 12L)
    assertEquals(count("copySource"), 12L)
    assertEquals(indexNames("copyAll"), Set("_id_", "compound", "uniqueI", "texts"))
    assertEquals(indexNames("copySource"), Set("_id_", "compound", "uniqueI", "texts"))
    val unique = provider.dao("copyAll").indexList().find(_.name == "uniqueI").get
    assertEquals(unique.unique, true)
    assertEquals(temporaryCollections, List())
  }

  test("only the documents that match the filter are copied") {
    val response = copy("copySource", """{"target": "copyFiltered", "filter": "type:a"}""")
    assertEquals(response.code.code, 200, response.body.fold(_.getMessage, _ => ""))
    assertEquals(response.body.toOption.flatMap(_.hcursor.get[Long]("count").toOption), Some(5L))
    assertEquals(count("copyFiltered"), 5L)
    assertEquals(indexNames("copyFiltered").contains("compound"), true)
  }

  test("a filter that matches nothing gives an empty copy") {
    assertEquals(copy("copySource", """{"target": "copyEmpty", "filter": "type:nothing"}""").code.code, 200)
    assertEquals(exists("copyEmpty"), true)
    assertEquals(count("copyEmpty"), 0L)
    assertEquals(indexNames("copyEmpty").contains("compound"), true)
  }

  test("without indices only the documents are copied") {
    assertEquals(copy("copySource", """{"target": "copyPlain", "withIndexes": false}""").code.code, 200)
    assertEquals(count("copyPlain"), 12L)
    assertEquals(indexNames("copyPlain"), Set("_id_"))
  }

  test("an existing target is a conflict and nothing changes") {
    val response = copy("copySource", """{"target": "copyPlain"}""")
    assertEquals(response.code.code, 409)
    assertEquals(response.header("x-error-code"), Some(ErrorCodes.collectionExists.toString))
    assertEquals(indexNames("copyPlain"), Set("_id_"))
    assertEquals(temporaryCollections, List())
  }

  test("with dropTarget the existing target is replaced by the complete copy") {
    val response = copy("copySource", """{"target": "copyPlain", "dropTarget": true, "filter": "type:b"}""")
    assertEquals(response.code.code, 200, response.body.fold(_.getMessage, _ => ""))
    assertEquals(count("copyPlain"), 7L)
    assertEquals(indexNames("copyPlain"), Set("_id_", "compound", "uniqueI", "texts"))
    assertEquals(temporaryCollections, List())
  }

  test("an invalid filter is a bad request and creates nothing") {
    val response = copy("copySource", """{"target": "copyBad", "filter": "type:("}""")
    assertEquals(response.code.code, 400)
    assertEquals(response.header("x-error-code"), Some("4001"))
    assertEquals(exists("copyBad"), false)
  }

  test("a failing copy leaves the target as it was and drops the temporary collection") {
    // a collection cannot be renamed onto a view: the copy is complete in the temporary collection when the database refuses the last step
    provider.database().createView("copyViewTarget", "copySource", Seq(Document("$limit" -> 1))).result()
    try {
      val response = copy("copySource", """{"target": "copyViewTarget", "dropTarget": true}""")
      assertEquals(response.code.code == 200, false, s"the copy onto a view worked: ${response.body.toString.take(200)}")
      assertEquals(exists("copyViewTarget"), true)
      assertEquals(temporaryCollections, List())
    }
    finally provider.database().getCollection("copyViewTarget").drop().result()
  }

  test("names that are not allowed are a bad request") {
    List("a$b", "docs.files", "system.views", "mc_mine", "other:target", "copySource").foreach(
      target => assertEquals(copy("copySource", s"""{"target": "$target"}""").code.code, 400, s"target `$target`")
    )
    List("mc_users", "sample-files.files").foreach(
      name => assertEquals(copy(name, """{"target": "stolen"}""").code.code, 400, name)
    )
    assertEquals(exists("stolen"), false)
  }

  test("a collection that does not exist is not found") {
    assertEquals(copy("copyMissing", """{"target": "copyOfMissing"}""").code.code, 404)
    assertEquals(exists("copyOfMissing"), false)
    assertEquals(temporaryCollections, List())
  }

  test("the read grant for the collection and the write grant for the target are needed") {
    val noWrite = copy("accounts", """{"target": "copyOfAccounts"}""", token = testUserBearerToken)
    assert(Set(401, 403).contains(noWrite.code.code), s"unexpected status ${noWrite.code.code}")
    assertEquals(exists("copyOfAccounts"), false)
    val noRead = copy("pokemon", """{"target": "users"}""", token = testUserBearerToken)
    assert(Set(401, 403).contains(noRead.code.code), s"unexpected status ${noRead.code.code}")
    // read grant for accounts, write grant for users: allowed, the target exists
    assertEquals(copy("accounts", """{"target": "users"}""", token = testUserBearerToken).code.code, 409)
  }

  test("cleanup") {
    cleanup("copySource", "copyAll", "copyFiltered", "copyEmpty", "copyPlain", "copyOfAccounts")
  }
}
