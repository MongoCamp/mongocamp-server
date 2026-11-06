package dev.mongocamp.server.tests

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.test.client.api.CollectionRenameApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import io.circe.Json
import io.circe.parser.parse
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.IndexOptions

// rename a collection in the database
class CollectionRenameSuite extends MongoCampBaseServerSuite {

  private lazy val renameApi = CollectionRenameApi()

  private def provider = MongoDatabase.databaseProvider

  private def rename(collection: String, body: String, token: String = adminBearerToken) =
    executeRequest(renameApi.renameCollection("", "", token, "")(collection, parse(body).getOrElse(Json.Null)))

  private def exists(name: String) = provider.collectionNames().contains(name)

  private def count(name: String) = provider.dao(name).count().result()

  private def fill(name: String, documents: Int, marker: String): Unit = {
    provider.dao(name).insertMany((1 to documents).map(i => Document("i" -> i, "marker" -> marker))).result()
    provider.dao(name).createIndex(Document("i" -> 1), IndexOptions().name(s"${name}_i")).result()
  }

  private def cleanup(names: String*): Unit = names.foreach(name => if (exists(name)) provider.dao(name).drop().result())

  test("a collection is renamed with its documents and indices") {
    fill("renameSource", 7, "source")
    val response = rename("renameSource", """{"target": "renameTarget"}""")
    assertEquals(response.code.code, 200, response.body.fold(_.getMessage, _ => ""))
    assertEquals(response.body.toOption.flatMap(_.hcursor.get[String]("name").toOption), Some("renameTarget"))
    assertEquals(exists("renameSource"), false)
    assertEquals(count("renameTarget"), 7L)
    assertEquals(provider.dao("renameTarget").indexList().map(_.name).toSet, Set("_id_", "renameSource_i"))
    cleanup("renameTarget")
  }

  test("an existing target is a conflict and nothing changes") {
    fill("renameA", 3, "a")
    fill("renameB", 5, "b")
    val response = rename("renameA", """{"target": "renameB"}""")
    assertEquals(response.code.code, 409)
    assertEquals(response.header("x-error-code"), Some(ErrorCodes.collectionExists.toString))
    assertEquals((count("renameA"), count("renameB")), (3L, 5L))
  }

  test("with dropTarget the existing target is replaced") {
    val response = rename("renameA", """{"target": "renameB", "dropTarget": true}""")
    assertEquals(response.code.code, 200, response.body.fold(_.getMessage, _ => ""))
    assertEquals(exists("renameA"), false)
    assertEquals(count("renameB"), 3L)
    cleanup("renameB")
  }

  test("a collection that does not exist is not found") {
    assertEquals(rename("renameMissing", """{"target": "renameWhatever"}""").code.code, 404)
    assertEquals(exists("renameWhatever"), false)
  }

  test("names that are not allowed are a bad request") {
    fill("renameBad", 1, "x")
    List("a$b", "docs.files", "system.views", "mc_mine", "other:target", "renameBad", " ").foreach(
      target => {
        val response = rename("renameBad", s"""{"target": "$target"}""")
        assertEquals(response.code.code, 400, s"target `$target`")
        assertEquals(response.header("x-error-code"), Some(ErrorCodes.invalidCollectionName.toString), s"target `$target`")
      }
    )
    assertEquals(exists("renameBad"), true)
    cleanup("renameBad")
  }

  test("the collections of the server and of a bucket cannot be renamed") {
    List("mc_users", "sample-files.files", "sample-files.chunks").foreach(
      name => assertEquals(rename(name, """{"target": "stolen"}""").code.code, 400, name)
    )
    assertEquals(exists("stolen"), false)
  }

  test("a user needs the administrate grant for the collection and the write grant for the target") {
    fill("test", 2, "t")
    // test: the user has the administrate grant, the target has no grant
    val withoutGrant = rename("test", """{"target": "renamedByUser"}""", token = testUserBearerToken)
    assert(Set(401, 403).contains(withoutGrant.code.code), s"unexpected status ${withoutGrant.code.code}")
    assertEquals(exists("renamedByUser"), false)
    // accounts: the user has no administrate grant
    val withoutAdminister = rename("accounts", """{"target": "test"}""", token = testUserBearerToken)
    assert(Set(401, 403).contains(withoutAdminister.code.code), s"unexpected status ${withoutAdminister.code.code}")
    assertEquals(exists("accounts"), true)
  }

  test("cleanup") {
    cleanup("renameSource", "renameTarget", "renameA", "renameB", "renameBad", "renamedByUser")
  }
}
