package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.client.api.DocumentsApi
import com.quadstingray.mongo.camp.client.model.{ MongoFindRequest, UpdateRequest }
import io.circe.syntax.EncoderOps

import java.util.UUID
import scala.util.Random

class DocumentsSuite extends BaseSuite {

  val documentsApi: DocumentsApi = DocumentsApi()
  var idForTest: String          = ""
  var idsForTest: List[String]   = List()

  test("list all documents as admin") {
    val response = executeRequestToResponse(documentsApi.listDocuments("", adminBearerToken)(collectionNameAccounts))
    assertEquals(response.size, 100)
    val fistDocument = response.head
    assertEquals(fistDocument.size, 4)
    assertEquals(fistDocument.contains("_id"), true)
    assertEquals(fistDocument("name"), "Dieter Houston")
    assertEquals(fistDocument("currency"), "$38.28")
    assertEquals(fistDocument("iban"), "PS706966638633086548675265681")
  }

  test("list filtered documents as admin") {
    val filter: String  = Map("iban" -> Map("$regex" -> "PL")).asJson.toString()
    val sort: String    = Map("currency" -> -1).asJson.toString()
    val project: String = Map("name" -> 1).asJson.toString()
    val response =
      executeRequest(documentsApi.listDocuments("", adminBearerToken)(collectionNameAccounts, Some(filter), Some(sort), Some(project), Some(2), Some(2)))
    val responseBody = response.body.getOrElse(throw new Exception("error"))
    assertEquals(responseBody.size, 1)
    val fistDocument = responseBody.head
    assertEquals(fistDocument.size, 2)
    assertEquals(fistDocument.keySet, Set("_id", "name"))
    assertEquals(fistDocument.contains("_id"), true)
    assertEquals(fistDocument("name"), "Jameson Sexton")
  }

  test("find all documents as admin") {
    val response = executeRequestToResponse(documentsApi.find("", adminBearerToken)(collectionNameAccounts, MongoFindRequest(Map(), Map(), Map())))
    assertEquals(response.size, 100)
    val fistDocument = response.head
    assertEquals(fistDocument.size, 4)
    assertEquals(fistDocument.contains("_id"), true)
    assertEquals(fistDocument("name"), "Dieter Houston")
    assertEquals(fistDocument("currency"), "$38.28")
    assertEquals(fistDocument("iban"), "PS706966638633086548675265681")
  }

  test("find filtered documents as admin") {
    val filter   = Map("iban" -> Map("$regex" -> "PL"))
    val sort     = Map("currency" -> -1)
    val project  = Map("name" -> 1)
    val response = executeRequest(documentsApi.find("", adminBearerToken)(collectionNameAccounts, MongoFindRequest(filter, sort, project), Some(2), Some(2)))
    val responseBody = response.body.getOrElse(throw new Exception("error"))
    assertEquals(responseBody.size, 1)
    val fistDocument = responseBody.head
    assertEquals(fistDocument.size, 2)
    assertEquals(fistDocument.keySet, Set("_id", "name"))
    assertEquals(fistDocument.contains("_id"), true)
    assertEquals(fistDocument("name"), "Jameson Sexton")
    assertEquals(response.header("x-pagination-rows-per-page"), Some("2"))
    assertEquals(response.header("x-pagination-current-page"), Some("2"))
    assertEquals(response.header("x-pagination-count-rows"), Some("3"))
    assertEquals(response.header("x-pagination-count-pages"), Some("2"))
  }

  test("create new document at database as admin") {
    val mapToInsert =
      Map("index" -> Random.nextInt(), "hello" -> "world", "uuid" -> UUID.randomUUID().toString, "name" -> Random.alphanumeric.take(10).mkString)
    val response = executeRequestToResponse(documentsApi.insert("", adminBearerToken)(collectionNameTest, mapToInsert))
    assertEquals(response.insertedIds.nonEmpty, true)
    assertEquals(response.insertedIds.size, 1)
    assertEquals(response.wasAcknowledged, true)
    idForTest = response.insertedIds.head
  }

  test("create new documents at database as admin") {
    val mapToInsert: List[Map[String, Any]] = (0 to 10)
      .map(i => Map("index" -> i, "hello" -> "world", "uuid" -> UUID.randomUUID().toString, "name" -> Random.alphanumeric.take(10).mkString))
      .toList
    val response = executeRequestToResponse(documentsApi.insertMany("", adminBearerToken)(collectionNameTest, mapToInsert))
    idsForTest = response.insertedIds.toList
    assertEquals(response.insertedIds.nonEmpty, true)
    assertEquals(response.insertedIds.size, 11)
    assertEquals(response.wasAcknowledged, true)
  }

  test("get document from database as admin") {
    val response = executeRequestToResponse(documentsApi.getDocument("", adminBearerToken)(collectionNameTest, idForTest))
    assertEquals(response.size, 5)
    assertEquals(response("_id"), idForTest)
    assertEquals(response("hello"), "world")
    assertEquals(response.contains("index"), true)
    assertEquals(response.contains("uuid"), true)
    assertEquals(response.contains("name"), true)
  }

  test("partial update documents from database as admin") {
    val request: Map[String, Any] = Map("hello" -> "you", "welcome" -> "world")
    val response                  = executeRequestToResponse(documentsApi.updateDocumentPartial("", adminBearerToken)(collectionNameTest, idForTest, request))
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.upsertedIds, List(idForTest))
    assertEquals(response.matchedCount, 1L)
    assertEquals(response.modifiedCount, 1L)
    val validation = executeRequestToResponse(documentsApi.getDocument("", adminBearerToken)(collectionNameTest, idForTest))
    assertEquals(validation.size, 6)
    assertEquals(validation("_id"), idForTest)
    assertEquals(validation("hello"), "you")
    assertEquals(validation("welcome"), "world")
    assertEquals(validation.contains("index"), true)
    assertEquals(validation.contains("uuid"), true)
    assertEquals(validation.contains("name"), true)
  }

  test("update document from database as admin") {
    val request: Map[String, Any] = Map("hello" -> "web", "welcome" -> "you")
    val response                  = executeRequestToResponse(documentsApi.updateDocument("", adminBearerToken)(collectionNameTest, idForTest, request))
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.upsertedIds, List(idForTest))
    assertEquals(response.matchedCount, 1L)
    assertEquals(response.modifiedCount, 1L)
    val validation = executeRequestToResponse(documentsApi.getDocument("", adminBearerToken)(collectionNameTest, idForTest))
    assertEquals(validation.size, 3)
    assertEquals(validation("_id"), idForTest)
    assertEquals(validation("hello"), "web")
    assertEquals(validation("welcome"), "you")
    assertEquals(validation.contains("index"), false)
    assertEquals(validation.contains("uuid"), false)
    assertEquals(validation.contains("name"), false)
  }

  test("update many documents from database as admin") {
    val request: UpdateRequest = UpdateRequest(Map("newField" -> "value"), Map())
    val response               = executeRequestToResponse(documentsApi.updateMany("", adminBearerToken)(collectionNameTest, request))
    assertEquals(response.modifiedCount, 12L)
    assertEquals(response.matchedCount, 12L)
    val validation = executeRequestToResponse(documentsApi.find("", adminBearerToken)(collectionNameTest, MongoFindRequest(Map(), Map(), Map())))
    validation.foreach(value => assertEquals(value.get("newField"), Some("value")))
  }

  test("delete document from database as admin") {
    val response = executeRequestToResponse(documentsApi.deleteDocument("", adminBearerToken)(collectionNameTest, idForTest))
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.deletedCount, 1L)
  }

  test("delete documents from database as admin") {
    val request  = Map("$or" -> idsForTest.splitAt(3)._1.map(value => Map("_id" -> value)))
    val response = executeRequestToResponse(documentsApi.deleteMany("", adminBearerToken)(collectionNameTest, request))
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.deletedCount, 3L)
  }

}
