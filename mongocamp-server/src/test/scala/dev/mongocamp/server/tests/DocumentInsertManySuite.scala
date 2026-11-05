package dev.mongocamp.server.tests

import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.client.api.DocumentCountApi
import dev.mongocamp.server.test.client.api.DocumentInsertManyApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite

// insert many with ordered=false: what can be stored is stored, the rest is reported with the position in the request
class DocumentInsertManySuite extends MongoCampBaseServerSuite {

  private lazy val insertApi    = DocumentInsertManyApi()
  private lazy val countApi     = DocumentCountApi()
  private lazy val documentsApi = DocumentApi()

  private val collection = "insertManyTest"

  private def insert(documents: Seq[Map[String, Any]], ordered: Option[Boolean], token: String = adminBearerToken, name: String = collection) =
    executeRequest(insertApi.insertMany("", "", token, "")(name, documents, ordered))

  private def count: Long = executeRequestToResponse(countApi.countDocuments("", "", adminBearerToken, "")(collection, None)).count

  test("prepare: documents with the ids 1 and 3") {
    executeRequestToResponse(documentsApi.insertMany("", "", adminBearerToken, "")(collection, Seq(Map("_id" -> "d1", "v" -> 0), Map("_id" -> "d3", "v" -> 0))))
    assertEquals(count, 2L)
  }

  test("ordered (default): the first duplicate fails the request, the documents before it are stored") {
    val response = insert(Seq(Map("_id" -> "o1", "v" -> 1), Map("_id" -> "d1", "v" -> 1), Map("_id" -> "o3", "v" -> 1)), None)
    assertEquals(response.code.code, 409)
    assertEquals(count, 3L)
  }

  test("unordered: the documents that can be stored are stored, the others are reported") {
    val before   = count
    val response = insert(Seq(Map("_id" -> "u1", "v" -> 2), Map("_id" -> "d1", "v" -> 2), Map("_id" -> "u3", "v" -> 2), Map("_id" -> "d3", "v" -> 2), Map("_id" -> "u5", "v" -> 2)), Some(false))
    assertEquals(response.code.code, 200, response.body.fold(_.getMessage, _ => ""))
    val body = response.body.getOrElse(throw new Exception("no body"))
    assertEquals(body.insertedIds.toSet, Set("u1", "u3", "u5"))
    assertEquals(body.insertedCount, Some(3L))
    assertEquals(body.errors.getOrElse(Nil).map(e => (e.index, e.code)), List((1, 11000), (3, 11000)))
    assertEquals(count, before + 3)
  }

  test("the errors do not reveal the database, the collection or the values") {
    val response = insert(Seq(Map("_id" -> "d1", "v" -> 3)), Some(false))
    val message  = response.body.toOption.flatMap(_.errors).flatMap(_.headOption).map(_.message).getOrElse("")
    assertEquals(message.isBlank, false)
    assertEquals(message.contains(collection), false, message)
    assertEquals(message.contains("test."), false, message)
    assertEquals(message.contains("dup key"), false, message)
  }

  test("unordered: documents without an id get one, the answer lists the ids of the stored documents") {
    val before   = count
    val response = insert(Seq(Map("v" -> 4), Map("_id" -> "d1", "v" -> 4), Map("v" -> 4)), Some(false))
    val body     = response.body.getOrElse(throw new Exception("no body"))
    assertEquals(body.insertedIds.size, 2)
    assert(body.insertedIds.forall(_.matches("[0-9a-f]{24}")), body.insertedIds.toString)
    assertEquals(body.errors.getOrElse(Nil).map(_.index), List(1))
    assertEquals(count, before + 2)
    body.insertedIds.foreach(
      id => assertEquals(executeRequest(documentsApi.getDocument("", "", adminBearerToken, "")(collection, id)).code.code, 200, id)
    )
  }

  test("unordered without a problem answers without errors") {
    val response = insert(Seq(Map("v" -> 5), Map("v" -> 5)), Some(false))
    val body     = response.body.getOrElse(throw new Exception("no body"))
    assertEquals((body.insertedCount, body.errors), (Some(2L), Some(Nil)))
  }

  test("an empty list is a bad request for both modes") {
    assertEquals(insert(Seq(), Some(false)).code.code, 400)
    assertEquals(insert(Seq(), None).code.code, 400)
  }

  test("ordered answers carry the count too") {
    val body = insert(Seq(Map("v" -> 6)), Some(true)).body.getOrElse(throw new Exception("no body"))
    assertEquals((body.insertedCount, body.errors), (Some(1L), Some(Nil)))
  }

  test("a user without a write grant cannot insert") {
    val code = insert(Seq(Map("v" -> 7)), Some(false), token = testUserBearerToken, name = "geodata:locations").code.code
    assert(Set(401, 403).contains(code), s"unexpected status $code")
  }

  test("cleanup") {
    executeRequestToResponse(CollectionApi().deleteCollection("", "", adminBearerToken, "")(collection))
  }
}
