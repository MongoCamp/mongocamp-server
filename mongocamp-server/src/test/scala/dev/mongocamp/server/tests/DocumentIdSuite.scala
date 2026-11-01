package dev.mongocamp.server.tests

import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.client.model.MongoFindRequest
import dev.mongocamp.server.test.client.model.UpdateRequest
import dev.mongocamp.server.test.MongoCampBaseServerSuite

// documents can have any value as `_id`, only the values that are ObjectIds may be converted
class DocumentIdSuite extends MongoCampBaseServerSuite {

  lazy val documentsApi: DocumentApi = DocumentApi()

  private val collection = "idTest"
  private val hexId      = "507f1f77bcf86cd799439011"

  private def find(filter: Map[String, Any]) =
    executeRequestToResponse(documentsApi.find("", "", adminBearerToken, "")(collection, MongoFindRequest(filter, Map(), Map())))

  private def ids(filter: Map[String, Any]): Set[String] = find(filter).map(_("_id").toString).toSet

  private def listByLucene(filter: String) = {
    val response = executeRequest(documentsApi.listDocuments("", "", adminBearerToken, "")(collection, Some(filter), Seq(), Seq(), Some(100), Some(1)))
    assertEquals(response.code.code, 200, s"filter `$filter` failed: ${response.body.fold(identity, _ => "")}")
    response.body.getOrElse(throw new Exception("error"))
  }

  test("insert a document with a string id") {
    val response = executeRequestToResponse(documentsApi.insert("", "", adminBearerToken, "")(collection, Map("_id" -> "doc-a", "name" -> "a")))
    assertEquals(response.insertedIds, Seq("doc-a"))
  }

  test("insert many documents with string, number and ObjectId ids") {
    val documents: Seq[Map[String, Any]] =
      Seq(Map("_id" -> "doc-b", "name" -> "b"), Map("_id" -> 42, "name" -> "number"), Map("_id" -> hexId, "name" -> "objectId"), Map("name" -> "generated"))
    val response = executeRequestToResponse(documentsApi.insertMany("", "", adminBearerToken, "")(collection, documents))
    assertEquals(response.insertedIds.size, 4)
    assertEquals(response.insertedIds.contains("doc-b"), true)
    assertEquals(response.insertedIds.contains("42"), true)
    assertEquals(response.insertedIds.contains(hexId), true)
  }

  test("get and update a document with a string id") {
    val document = executeRequestToResponse(documentsApi.getDocument("", "", adminBearerToken, "")(collection, "doc-a"))
    assertEquals(document("_id"), "doc-a")
    val partial = executeRequestToResponse(documentsApi.updateDocumentPartial("", "", adminBearerToken, "")(collection, "doc-a", Map("partial" -> 1)))
    assertEquals(partial.matchedCount, 1L)
    assertEquals(partial.modifiedCount, 1L)
    assertEquals(partial.upsertedIds, List("doc-a"))
    val replaced = executeRequestToResponse(documentsApi.update("", "", adminBearerToken, "")(collection, "doc-a", Map("name" -> "a", "replaced" -> true)))
    assertEquals(replaced.matchedCount, 1L)
    assertEquals(replaced.upsertedIds, List("doc-a"))
    assertEquals(executeRequestToResponse(documentsApi.getDocument("", "", adminBearerToken, "")(collection, "doc-a")).get("replaced"), Some(true))
  }

  test("filter on _id with $in, $oid and $eq") {
    assertEquals(ids(Map("_id" -> Map("$in" -> List("doc-a", "doc-b")))), Set("doc-a", "doc-b"))
    assertEquals(ids(Map("_id" -> Map("$oid" -> hexId))), Set(hexId))
    assertEquals(ids(Map("_id" -> Map("$in" -> List(hexId, "doc-a")))), Set(hexId, "doc-a"))
    assertEquals(ids(Map("_id" -> Map("$eq" -> "doc-b"))), Set("doc-b"))
    assertEquals(ids(Map("_id" -> "doc-b")), Set("doc-b"))
    assertEquals(ids(Map("_id" -> hexId)), Set(hexId))
    assertEquals(ids(Map("$or" -> List(Map("_id" -> "doc-a"), Map("_id" -> hexId)))), Set("doc-a", hexId))
  }

  test("an id that does not exist finds nothing") {
    assertEquals(find(Map("_id" -> Map("$in" -> List("abc", "not-an-object-id")))).size, 0)
    assertEquals(find(Map("_id" -> "abc")).size, 0)
  }

  test("lucene filter on _id") {
    assertEquals(listByLucene(s"_id:$hexId").map(_("_id").toString), List(hexId))
    assertEquals(listByLucene(s"_id:\"$hexId\"").map(_("_id").toString), List(hexId))
    // the driver converts `field:*` to a $regex, which does not match an ObjectId (MongoCamp/mongodb-driver#86), but it must not fail
    listByLucene("_id:*")
  }

  test("update many and delete many with a filter on _id") {
    val updated = executeRequestToResponse(
      documentsApi.updateMany("", "", adminBearerToken, "")(collection, UpdateRequest(Map("flag" -> true), Map("_id" -> Map("$in" -> List("doc-a", "doc-b")))))
    )
    assertEquals(updated.matchedCount, 2L)
    assertEquals(updated.modifiedCount, 2L)
    val deleted =
      executeRequestToResponse(documentsApi.deleteMany("", "", adminBearerToken, "")(collection, Map("_id" -> Map("$in" -> List("doc-a", "doc-b")))))
    assertEquals(deleted.deletedCount, 2L)
    assertEquals(find(Map()).size, 3)
  }

  test("delete a document by an ObjectId id and by a string id") {
    val deletedObjectId = executeRequestToResponse(documentsApi.delete("", "", adminBearerToken, "")(collection, hexId))
    assertEquals(deletedObjectId.deletedCount, 1L)
    executeRequestToResponse(documentsApi.insert("", "", adminBearerToken, "")(collection, Map("_id" -> "doc-c", "name" -> "c")))
    val deletedString = executeRequestToResponse(documentsApi.delete("", "", adminBearerToken, "")(collection, "doc-c"))
    assertEquals(deletedString.deletedCount, 1L)
  }

  test("insert many with an empty list answers 400") {
    val response = executeRequest(documentsApi.insertMany("", "", adminBearerToken, "")(collection, Seq()))
    assertEquals(response.code.code, 400)
    assertEquals(response.body.left.map(_.getMessage).left.getOrElse("").contains("empty"), true)
  }

  test("insert a document with an existing id answers 409") {
    executeRequestToResponse(documentsApi.insert("", "", adminBearerToken, "")(collection, Map("_id" -> "doc-dup", "name" -> "first")))
    val single = executeRequest(documentsApi.insert("", "", adminBearerToken, "")(collection, Map("_id" -> "doc-dup", "name" -> "second")))
    assertEquals(single.code.code, 409)
    val many = executeRequest(documentsApi.insertMany("", "", adminBearerToken, "")(collection, Seq(Map("_id" -> "doc-dup", "name" -> "third"))))
    assertEquals(many.code.code, 409)
    assertEquals(executeRequestToResponse(documentsApi.delete("", "", adminBearerToken, "")(collection, "doc-dup")).deletedCount, 1L)
  }
}
