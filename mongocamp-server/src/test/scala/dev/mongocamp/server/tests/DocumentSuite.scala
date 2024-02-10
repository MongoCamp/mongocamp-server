package dev.mongocamp.server.tests
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.client.model.{ MongoFindRequest, UpdateRequest }

import java.util.UUID
import scala.collection.mutable
import scala.util.Random

class DocumentSuite extends MongoCampBaseServerSuite {

  lazy val documentsApi: DocumentApi = DocumentApi()
  var idForTest: String              = ""
  var idsForTest: List[String]       = List()

  test("list all documents as admin") {
    val response = executeRequestToResponse(documentsApi.listDocuments("", "", adminBearerToken, "")(collectionNameAccounts, None, Seq.empty, Seq.empty))
    assertEquals(response.size, 100)
    val fistDocument = response.head
    assertEquals(fistDocument.size, 4)
    assertEquals(fistDocument.contains("_id"), true)
    assertEquals(fistDocument("name"), "Dieter Houston")
    assertEquals(fistDocument("currency"), "$38.28")
    assertEquals(fistDocument("iban"), "PS706966638633086548675265681")
  }

  test("list filtered documents as admin") {
    val filter: String        = "iban: *PL*"
    val sort: List[String]    = List("-currency")
    val project: List[String] = List("name")
    val response =
      executeRequest(documentsApi.listDocuments("", "", adminBearerToken, "")(collectionNameAccounts, Some(filter), sort, project, Some(2), Some(2)))
    val responseBody = response.body.getOrElse(throw new Exception("error"))
    assertEquals(responseBody.size, 1)
    val fistDocument = responseBody.head
    assertEquals(fistDocument.size, 2)
    assertEquals(fistDocument.keySet, Set("_id", "name"))
    assertEquals(fistDocument.contains("_id"), true)
    assertEquals(fistDocument("name"), "Jameson Sexton")
  }

  test("find all documents as admin") {
    val response = executeRequestToResponse(documentsApi.find("", "", adminBearerToken, "")(collectionNameAccounts, MongoFindRequest(Map(), Map(), Map())))
    assertEquals(response.size, 100)
    val fistDocument = response.head
    assertEquals(fistDocument.size, 4)
    assertEquals(fistDocument.contains("_id"), true)
    assertEquals(fistDocument("name"), "Dieter Houston")
    assertEquals(fistDocument("currency"), "$38.28")
    assertEquals(fistDocument("iban"), "PS706966638633086548675265681")
  }

  test("find filtered documents as admin") {
    val filter  = Map("iban" -> Map("$regex" -> "PL"))
    val sort    = Map("currency" -> -1)
    val project = Map("name" -> 1)
    val response =
      executeRequest(documentsApi.find("", "", adminBearerToken, "")(collectionNameAccounts, MongoFindRequest(filter, sort, project), Some(2), Some(2)))
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
    val response = executeRequestToResponse(documentsApi.insert("", "", adminBearerToken, "")(collectionNameTest, mapToInsert))
    assertEquals(response.insertedIds.nonEmpty, true)
    assertEquals(response.insertedIds.size, 1)
    assertEquals(response.wasAcknowledged, true)
    idForTest = response.insertedIds.head
  }

  test("create new documents at database as admin") {
    val mapToInsert: List[Map[String, Any]] = (0 to 10)
      .map(
        i => Map("index" -> i, "hello" -> "world", "uuid" -> UUID.randomUUID().toString, "name" -> Random.alphanumeric.take(10).mkString)
      )
      .toList
    val response = executeRequestToResponse(documentsApi.insertMany("", "", adminBearerToken, "")(collectionNameTest, mapToInsert))
    idsForTest = response.insertedIds.toList
    assertEquals(response.insertedIds.nonEmpty, true)
    assertEquals(response.insertedIds.size, 11)
    assertEquals(response.wasAcknowledged, true)
  }

  test("get document from database as admin") {
    val response = executeRequestToResponse(documentsApi.getDocument("", "", adminBearerToken, "")(collectionNameTest, idForTest))
    assertEquals(response.size, 5)
    assertEquals(response("_id"), idForTest)
    assertEquals(response("hello"), "world")
    assertEquals(response.contains("index"), true)
    assertEquals(response.contains("uuid"), true)
    assertEquals(response.contains("name"), true)
  }

  test("partial update documents from database as admin") {
    val request: Map[String, Any] = Map("hello" -> "you", "welcome" -> "world")
    val response = executeRequestToResponse(documentsApi.updateDocumentPartial("", "", adminBearerToken, "")(collectionNameTest, idForTest, request))
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.upsertedIds, List(idForTest))
    assertEquals(response.matchedCount, 1L)
    assertEquals(response.modifiedCount, 1L)
    val validation = executeRequestToResponse(documentsApi.getDocument("", "", adminBearerToken, "")(collectionNameTest, idForTest))
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
    val response                  = executeRequestToResponse(documentsApi.update("", "", adminBearerToken, "")(collectionNameTest, idForTest, request))
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.upsertedIds, List(idForTest))
    assertEquals(response.matchedCount, 1L)
    assertEquals(response.modifiedCount, 1L)
    val validation = executeRequestToResponse(documentsApi.getDocument("", "", adminBearerToken, "")(collectionNameTest, idForTest))
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
    val response               = executeRequestToResponse(documentsApi.updateMany("", "", adminBearerToken, "")(collectionNameTest, request))
    assertEquals(response.modifiedCount, 12L)
    assertEquals(response.matchedCount, 12L)
    val validation = executeRequestToResponse(documentsApi.find("", "", adminBearerToken, "")(collectionNameTest, MongoFindRequest(Map(), Map(), Map())))
    validation.foreach(
      value => assertEquals(value.get("newField"), Some("value"))
    )
  }

  test("delete document from database as admin") {
    val response = executeRequestToResponse(documentsApi.delete("", "", adminBearerToken, "")(collectionNameTest, idForTest))
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.deletedCount, 1L)
  }

  test("delete documents from database as admin") {
    val request = Map(
      "$or" -> idsForTest
        .splitAt(3)
        ._1
        .map(
          value => Map("_id" -> value)
        )
    )
    val response = executeRequestToResponse(documentsApi.deleteMany("", "", adminBearerToken, "")(collectionNameTest, request))
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.deletedCount, 3L)
  }

  test("list all documents as user") {
    val response = executeRequestToResponse(documentsApi.listDocuments("", "", testUserBearerToken, "")(collectionNameAccounts, None, Seq.empty, Seq.empty))
    assertEquals(response.size, 100)
    val fistDocument = response.head
    assertEquals(fistDocument.size, 4)
    assertEquals(fistDocument.contains("_id"), true)
    assertEquals(fistDocument("name"), "Dieter Houston")
    assertEquals(fistDocument("currency"), "$38.28")
    assertEquals(fistDocument("iban"), "PS706966638633086548675265681")
  }

  test("list filtered documents as user") {
    val filter: String        = "iban: *PL*"
    val sort: List[String]    = List("-currency")
    val project: List[String] = List("name")
    val response =
      executeRequest(documentsApi.listDocuments("", "", testUserBearerToken, "")(collectionNameAccounts, Some(filter), sort, project, Some(2), Some(2)))
    val responseBody = response.body.getOrElse(throw new Exception("error"))
    assertEquals(responseBody.size, 1)
    val fistDocument = responseBody.head
    assertEquals(fistDocument.size, 2)
    assertEquals(fistDocument.keySet, Set("_id", "name"))
    assertEquals(fistDocument.contains("_id"), true)
    assertEquals(fistDocument("name"), "Jameson Sexton")
  }

  test("find all documents as user") {
    val response = executeRequestToResponse(documentsApi.find("", "", testUserBearerToken, "")(collectionNameAccounts, MongoFindRequest(Map(), Map(), Map())))
    assertEquals(response.size, 100)
    val fistDocument = response.head
    assertEquals(fistDocument.size, 4)
    assertEquals(fistDocument.contains("_id"), true)
    assertEquals(fistDocument("name"), "Dieter Houston")
    assertEquals(fistDocument("currency"), "$38.28")
    assertEquals(fistDocument("iban"), "PS706966638633086548675265681")
  }

  test("find filtered documents as user") {
    val filter  = Map("iban" -> Map("$regex" -> "PL"))
    val sort    = Map("currency" -> -1)
    val project = Map("name" -> 1)
    val response =
      executeRequest(documentsApi.find("", "", testUserBearerToken, "")(collectionNameAccounts, MongoFindRequest(filter, sort, project), Some(2), Some(2)))
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

  test("create new document at database as user") {
    val mapToInsert =
      Map("index" -> Random.nextInt(), "hello" -> "world", "uuid" -> UUID.randomUUID().toString, "name" -> Random.alphanumeric.take(10).mkString)
    val response = executeRequestToResponse(documentsApi.insert("", "", testUserBearerToken, "")(collectionNameTest, mapToInsert))
    assertEquals(response.insertedIds.nonEmpty, true)
    assertEquals(response.insertedIds.size, 1)
    assertEquals(response.wasAcknowledged, true)
    idForTest = response.insertedIds.head
  }

  test("create new documents at database as user") {
    val mapToInsert: List[Map[String, Any]] = (0 to 10)
      .map(
        i => Map("index" -> i, "hello" -> "world", "uuid" -> UUID.randomUUID().toString, "name" -> Random.alphanumeric.take(10).mkString)
      )
      .toList
    val response = executeRequestToResponse(documentsApi.insertMany("", "", testUserBearerToken, "")(collectionNameTest, mapToInsert))
    idsForTest = response.insertedIds.toList
    assertEquals(response.insertedIds.nonEmpty, true)
    assertEquals(response.insertedIds.size, 11)
    assertEquals(response.wasAcknowledged, true)
  }

  test("get document from database as user") {
    val response = executeRequestToResponse(documentsApi.getDocument("", "", testUserBearerToken, "")(collectionNameTest, idForTest))
    assertEquals(response.size, 5)
    assertEquals(response("_id"), idForTest)
    assertEquals(response("hello"), "world")
    assertEquals(response.contains("index"), true)
    assertEquals(response.contains("uuid"), true)
    assertEquals(response.contains("name"), true)
  }

  test("partial update documents from database as user") {
    val request: Map[String, Any] = Map("hello" -> "you", "welcome" -> "world")
    val response = executeRequestToResponse(documentsApi.updateDocumentPartial("", "", testUserBearerToken, "")(collectionNameTest, idForTest, request))
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.upsertedIds, List(idForTest))
    assertEquals(response.matchedCount, 1L)
    assertEquals(response.modifiedCount, 1L)
    val validation = executeRequestToResponse(documentsApi.getDocument("", "", testUserBearerToken, "")(collectionNameTest, idForTest))
    assertEquals(validation.size, 6)
    assertEquals(validation("_id"), idForTest)
    assertEquals(validation("hello"), "you")
    assertEquals(validation("welcome"), "world")
    assertEquals(validation.contains("index"), true)
    assertEquals(validation.contains("uuid"), true)
    assertEquals(validation.contains("name"), true)
  }

  test("update document from database as user") {
    val request: Map[String, Any] = Map("hello" -> "web", "welcome" -> "you")
    val response                  = executeRequestToResponse(documentsApi.update("", "", testUserBearerToken, "")(collectionNameTest, idForTest, request))
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.upsertedIds, List(idForTest))
    assertEquals(response.matchedCount, 1L)
    assertEquals(response.modifiedCount, 1L)
    val validation = executeRequestToResponse(documentsApi.getDocument("", "", testUserBearerToken, "")(collectionNameTest, idForTest))
    assertEquals(validation.size, 3)
    assertEquals(validation("_id"), idForTest)
    assertEquals(validation("hello"), "web")
    assertEquals(validation("welcome"), "you")
    assertEquals(validation.contains("index"), false)
    assertEquals(validation.contains("uuid"), false)
    assertEquals(validation.contains("name"), false)
  }

  test("update many documents from database as user") {
    val request: UpdateRequest = UpdateRequest(Map("newField" -> "value"), Map())
    val response               = executeRequestToResponse(documentsApi.updateMany("", "", testUserBearerToken, "")(collectionNameTest, request))
    assertEquals(response.modifiedCount, 12L)
    assertEquals(response.matchedCount, 20L)
    val validation = executeRequestToResponse(documentsApi.find("", "", testUserBearerToken, "")(collectionNameTest, MongoFindRequest(Map(), Map(), Map())))
    validation.foreach(
      value => assertEquals(value.get("newField"), Some("value"))
    )
  }

  test("delete document from database as user") {
    val response = executeRequestToResponse(documentsApi.delete("", "", testUserBearerToken, "")(collectionNameTest, idForTest))
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.deletedCount, 1L)
  }

  test("delete documents from database as user") {
    val request = Map(
      "$or" -> idsForTest
        .splitAt(3)
        ._1
        .map(
          value => Map("_id" -> value)
        )
    )
    val response = executeRequestToResponse(documentsApi.deleteMany("", "", testUserBearerToken, "")(collectionNameTest, request))
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.deletedCount, 3L)
  }

  val notAllowedCollectionName = "notAllowedCollection"
  test("list all documents as user not allowed") {
    val response = executeRequest(documentsApi.listDocuments("", "", testUserBearerToken, "")(notAllowedCollectionName, None, Seq.empty, Seq.empty))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("list filtered documents as user not allowed") {
    val filter: String        = "iban: *PL*"
    val sort: List[String]    = List("-currency")
    val project: List[String] = List("name")
    val response =
      executeRequest(documentsApi.listDocuments("", "", testUserBearerToken, "")(notAllowedCollectionName, Some(filter), sort, project, Some(2), Some(2)))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("find all documents as user not allowed") {
    val response = executeRequest(documentsApi.find("", "", testUserBearerToken, "")(notAllowedCollectionName, MongoFindRequest(Map(), Map(), Map())))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("find filtered documents as user not allowed") {
    val filter  = Map("iban" -> Map("$regex" -> "PL"))
    val sort    = Map("currency" -> -1)
    val project = Map("name" -> 1)
    val response =
      executeRequest(documentsApi.find("", "", testUserBearerToken, "")(notAllowedCollectionName, MongoFindRequest(filter, sort, project), Some(2), Some(2)))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("create new document at database as user not allowed") {
    val mapToInsert =
      Map("index" -> Random.nextInt(), "hello" -> "world", "uuid" -> UUID.randomUUID().toString, "name" -> Random.alphanumeric.take(10).mkString)
    val response = executeRequest(documentsApi.insert("", "", testUserBearerToken, "")(notAllowedCollectionName, mapToInsert))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("create new documents at database as user not allowed") {
    val mapToInsert: List[Map[String, Any]] = (0 to 10)
      .map(
        i => Map("index" -> i, "hello" -> "world", "uuid" -> UUID.randomUUID().toString, "name" -> Random.alphanumeric.take(10).mkString)
      )
      .toList
    val response = executeRequest(documentsApi.insertMany("", "", testUserBearerToken, "")(notAllowedCollectionName, mapToInsert))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("get document from database as user not allowed") {
    val response = executeRequest(documentsApi.getDocument("", "", testUserBearerToken, "")(notAllowedCollectionName, idForTest))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("partial update documents from database as user not allowed") {
    val request: Map[String, Any] = Map("hello" -> "you", "welcome" -> "world")
    val response = executeRequest(documentsApi.updateDocumentPartial("", "", testUserBearerToken, "")(notAllowedCollectionName, idForTest, request))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("update document from database as user not allowed") {
    val request: Map[String, Any] = Map("hello" -> "web", "welcome" -> "you")
    val response                  = executeRequest(documentsApi.update("", "", testUserBearerToken, "")(notAllowedCollectionName, idForTest, request))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("update many documents from database as user not allowed") {
    val request: UpdateRequest = UpdateRequest(Map("newField" -> "value"), Map())
    val response               = executeRequest(documentsApi.updateMany("", "", testUserBearerToken, "")(notAllowedCollectionName, request))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("delete document from database as user not allowed") {
    val response = executeRequest(documentsApi.delete("", "", testUserBearerToken, "")(notAllowedCollectionName, idForTest))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("delete documents from database as user not allowed") {
    val request = Map(
      "$or" -> idsForTest
        .splitAt(3)
        ._1
        .map(
          value => Map("_id" -> value)
        )
    )
    val response = executeRequest(documentsApi.deleteMany("", "", testUserBearerToken, "")(notAllowedCollectionName, request))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for collection")
  }

  test("document with simple list document") {
    val response = executeRequestToResponse(
      documentsApi.find("", "", testUserBearerToken, "")(collectionNameUsers, MongoFindRequest(Map("email" -> "varius.ultrices@icloud.net"), Map(), Map()))
    )
    assertEquals(response.head("list").asInstanceOf[List[Long]], List(47L, 3L, 71L))
  }

  test("document with complex list document") {
    val response = executeRequestToResponse(
      documentsApi.find("", "", adminBearerToken, "")(MongoDatabase.CollectionNameRoles, MongoFindRequest(Map("name" -> "adminRole"), Map(), Map()))
    )
    val grantsList = List(
      Map("name" -> "*", "read" -> true, "write" -> true, "administrate" -> true, "grantType" -> "COLLECTION"),
      Map("name" -> "*", "read" -> true, "write" -> true, "administrate" -> true, "grantType" -> "BUCKET")
    )
    assertEquals(response.head("collectionGrants").asInstanceOf[List[Map[String, Any]]], grantsList)
  }

  test("create and update document with date") {
    val collectionNameTest = "createAndUpdate"
    val mapToInsert: mutable.Map[String, Any] = mutable.Map(
      "number" -> 1234,
      "metaData" -> Map(
        "createdBy" -> "tom@sfxcode.com",
        "updatedBy" -> "tom@sfxcode.com",
        "created"   -> "2023-04-12T16:32:01.452Z",
        "updated"   -> "2023-04-12T16:33:07.982Z"
      ),
      "name" -> "test1"
    )
    val insertResponse = executeRequestToResponse(documentsApi.insert("", "", adminBearerToken, "")(collectionNameTest, mapToInsert.toMap))
    val id             = insertResponse.insertedIds.head
    assertEquals(insertResponse.insertedIds.nonEmpty, true)
    assertEquals(insertResponse.insertedIds.size, 1)
    assertEquals(insertResponse.wasAcknowledged, true)

    val checkAfterInsert = executeRequestToResponse(documentsApi.getDocument("", "", adminBearerToken, "")(collectionNameTest, id))
    assertEquals(checkAfterInsert("number"), 1234)
    assertEquals(checkAfterInsert("metaData").asInstanceOf[Map[String, Any]]("created").toString, "2023-04-12T18:32:01.452+02:00")
    mapToInsert.put("number", 5678)
    val updateResponse = executeRequestToResponse(documentsApi.update("", "", adminBearerToken, "")(collectionNameTest, id, mapToInsert.toMap))
    assertEquals(updateResponse.upsertedIds.nonEmpty, true)
    assertEquals(updateResponse.upsertedIds.size, 1)
    assertEquals(updateResponse.wasAcknowledged, true)

    val checkAfterUpdate = executeRequestToResponse(documentsApi.getDocument("", "", adminBearerToken, "")(collectionNameTest, id))
    assertEquals(checkAfterUpdate("number"), 5678)
    assertEquals(checkAfterUpdate("metaData").asInstanceOf[Map[String, Any]]("created").toString, "2023-04-12T18:32:01.452+02:00")
  }

  test("create or update a document with a sublist of type string") {
    val collectionNameTest = "createAndUpdate"
    val mapToInsert: mutable.Map[String, Any] = mutable.Map(
      "number" -> 1234,
      "metaData" -> Map(
        "createdBy" -> "tom@sfxcode.com",
        "updatedBy" -> "tom@sfxcode.com",
        "created"   -> "2023-04-12T16:32:01.452Z",
        "updated"   -> "2023-04-12T16:33:07.982Z"
      ),
      "name" -> "test1",
      "list" -> List("hello", "men")
    )
    val insertResponse = executeRequestToResponse(documentsApi.insert("", "", adminBearerToken, "")(collectionNameTest, mapToInsert.toMap))
    val id             = insertResponse.insertedIds.head
    assertEquals(insertResponse.insertedIds.nonEmpty, true)
    assertEquals(insertResponse.insertedIds.size, 1)
    assertEquals(insertResponse.wasAcknowledged, true)

    val checkAfterInsert = executeRequestToResponse(documentsApi.getDocument("", "", adminBearerToken, "")(collectionNameTest, id))
    assertEquals(checkAfterInsert("number"), 1234)
    assertEquals(checkAfterInsert("metaData").asInstanceOf[Map[String, Any]]("created").toString, "2023-04-12T18:32:01.452+02:00")
    assertEquals(checkAfterInsert("list"), List("hello", "men"))
    mapToInsert.put("number", 5678)
    mapToInsert.put("list", List("hello", "world"))

    val updateResponse = executeRequestToResponse(documentsApi.update("", "", adminBearerToken, "")(collectionNameTest, id, mapToInsert.toMap))
    assertEquals(updateResponse.upsertedIds.nonEmpty, true)
    assertEquals(updateResponse.upsertedIds.size, 1)
    assertEquals(updateResponse.wasAcknowledged, true)

    val checkAfterUpdate = executeRequestToResponse(documentsApi.getDocument("", "", adminBearerToken, "")(collectionNameTest, id))
    assertEquals(checkAfterUpdate("number"), 5678)
    assertEquals(checkAfterUpdate("metaData").asInstanceOf[Map[String, Any]]("created").toString, "2023-04-12T18:32:01.452+02:00")
    assertEquals(checkAfterUpdate("list"), List("hello", "world"))

  }

  test("create or update a document with a sublist of type number") {
    val collectionNameTest = "createAndUpdate"
    val mapToInsert: mutable.Map[String, Any] = mutable.Map(
      "number" -> 1234,
      "metaData" -> Map(
        "createdBy" -> "tom@sfxcode.com",
        "updatedBy" -> "tom@sfxcode.com",
        "created"   -> "2023-04-12T16:32:01.452Z",
        "updated"   -> "2023-04-12T16:33:07.982Z"
      ),
      "name" -> "test1",
      "list" -> List(123, 456)
    )
    val insertResponse = executeRequestToResponse(documentsApi.insert("", "", adminBearerToken, "")(collectionNameTest, mapToInsert.toMap))
    val id             = insertResponse.insertedIds.head
    assertEquals(insertResponse.insertedIds.nonEmpty, true)
    assertEquals(insertResponse.insertedIds.size, 1)
    assertEquals(insertResponse.wasAcknowledged, true)

    val checkAfterInsert = executeRequestToResponse(documentsApi.getDocument("", "", adminBearerToken, "")(collectionNameTest, id))
    assertEquals(checkAfterInsert("number"), 1234)
    assertEquals(checkAfterInsert("metaData").asInstanceOf[Map[String, Any]]("created").toString, "2023-04-12T18:32:01.452+02:00")
    assertEquals(checkAfterInsert("list"), List(123, 456))
    mapToInsert.put("number", 5678)
    mapToInsert.put("list", List(789, 987))

    val updateResponse = executeRequestToResponse(documentsApi.update("", "", adminBearerToken, "")(collectionNameTest, id, mapToInsert.toMap))
    assertEquals(updateResponse.upsertedIds.nonEmpty, true)
    assertEquals(updateResponse.upsertedIds.size, 1)
    assertEquals(updateResponse.wasAcknowledged, true)

    val checkAfterUpdate = executeRequestToResponse(documentsApi.getDocument("", "", adminBearerToken, "")(collectionNameTest, id))
    assertEquals(checkAfterUpdate("number"), 5678)
    assertEquals(checkAfterUpdate("metaData").asInstanceOf[Map[String, Any]]("created").toString, "2023-04-12T18:32:01.452+02:00")
    assertEquals(checkAfterUpdate("list"), List(789, 987))

  }

  test("create or update a document with a sublist of type map") {
    val collectionNameTest = "createAndUpdate"
    val mapToInsert: mutable.Map[String, Any] = mutable.Map(
      "number" -> 1234,
      "metaData" -> Map(
        "createdBy" -> "tom@sfxcode.com",
        "updatedBy" -> "tom@sfxcode.com",
        "created"   -> "2023-04-12T16:32:01.452Z",
        "updated"   -> "2023-04-12T16:33:07.982Z"
      ),
      "name" -> "test1",
      "list" -> List(Map("a" -> "A"), Map("b" -> "B"))
    )
    val insertResponse = executeRequestToResponse(documentsApi.insert("", "", adminBearerToken, "")(collectionNameTest, mapToInsert.toMap))
    val id             = insertResponse.insertedIds.head
    assertEquals(insertResponse.insertedIds.nonEmpty, true)
    assertEquals(insertResponse.insertedIds.size, 1)
    assertEquals(insertResponse.wasAcknowledged, true)

    val checkAfterInsert = executeRequestToResponse(documentsApi.getDocument("", "", adminBearerToken, "")(collectionNameTest, id))
    assertEquals(checkAfterInsert("number"), 1234)
    assertEquals(checkAfterInsert("metaData").asInstanceOf[Map[String, Any]]("created").toString, "2023-04-12T18:32:01.452+02:00")
    assertEquals(checkAfterInsert("list"), List(Map("a" -> "A"), Map("b" -> "B")))
    mapToInsert.put("number", 5678)
    mapToInsert.put("list", List(Map("c" -> "C"), Map("d" -> "D")))

    val updateResponse = executeRequestToResponse(documentsApi.update("", "", adminBearerToken, "")(collectionNameTest, id, mapToInsert.toMap))
    assertEquals(updateResponse.upsertedIds.nonEmpty, true)
    assertEquals(updateResponse.upsertedIds.size, 1)
    assertEquals(updateResponse.wasAcknowledged, true)

    val checkAfterUpdate = executeRequestToResponse(documentsApi.getDocument("", "", adminBearerToken, "")(collectionNameTest, id))
    assertEquals(checkAfterUpdate("number"), 5678)
    assertEquals(checkAfterUpdate("metaData").asInstanceOf[Map[String, Any]]("created").toString, "2023-04-12T18:32:01.452+02:00")
    assertEquals(checkAfterUpdate("list"), List(Map("c" -> "C"), Map("d" -> "D")))

  }
}
