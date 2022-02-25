package com.quadstingray.mongo.camp.tests

import com.quadstingray.mongo.camp.client.api.CreateApi
import com.quadstingray.mongo.camp.server.TestAdditions

import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Random

class CreateSuite extends BaseSuite {

  val api: CreateApi = CreateApi()

  test("create new document at database as admin") {
    val mapToInsert = Map("index" -> Random.nextInt(), "hallo" -> "welt", "uuid" -> UUID.randomUUID().toString, "name" -> Random.alphanumeric.take(10).mkString)
    val resultFuture   = TestAdditions.backend.send(api.insert("", adminBearerToken)("admin-test", mapToInsert))
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.insertedIds.isDefined, true)
    assertEquals(response.insertedIds.get.size, 1)
    assertEquals(response.wasAcknowledged, true)
  }

  test("create new documents at database as admin") {
    val mapToInsert: List[Map[String, Any]] = (0 to 10)
      .map(i => Map("index" -> i, "hallo" -> "welt", "uuid" -> UUID.randomUUID().toString, "name" -> Random.alphanumeric.take(10).mkString))
      .toList

    val resultFuture   = TestAdditions.backend.send(api.insertMany("", adminBearerToken)("admin-test", mapToInsert))
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.insertedIds.isDefined, true)
    assertEquals(response.insertedIds.get.size, 11)
    assertEquals(response.wasAcknowledged, true)
  }

  test("create new document at database as user") {
    val mapToInsert = Map("index" -> Random.nextInt(), "hallo" -> "welt", "uuid" -> UUID.randomUUID().toString, "name" -> Random.alphanumeric.take(10).mkString)
    val resultFuture   = TestAdditions.backend.send(api.insert("", testUserBearerToken)("test", mapToInsert))
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.insertedIds.isDefined, true)
    assertEquals(response.insertedIds.get.size, 1)
    assertEquals(response.wasAcknowledged, true)
  }

  test("create new documents at database as user") {
    val mapToInsert: List[Map[String, Any]] = (0 to 10)
      .map(i => Map("index" -> i, "hallo" -> "welt", "uuid" -> UUID.randomUUID().toString, "name" -> Random.alphanumeric.take(10).mkString))
      .toList

    val resultFuture   = TestAdditions.backend.send(api.insertMany("", testUserBearerToken)("test", mapToInsert))
    val responseResult = Await.result(resultFuture, 1.seconds)
    val response       = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.insertedIds.isDefined, true)
    assertEquals(response.insertedIds.get.size, 11)
    assertEquals(response.wasAcknowledged, true)
  }

  test("try create new document at database for restricted collection as user") {
    val mapToInsert    = Map("hallo" -> "welt", "uuid" -> UUID.randomUUID().toString, "name" -> Random.alphanumeric.take(10).mkString)
    val resultFuture   = TestAdditions.backend.send(api.insert("", testUserBearerToken)(Random.alphanumeric.take(10).mkString, mapToInsert))
    val responseResult = Await.result(resultFuture, 1.seconds)
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for collection")
  }

  test("try create new documents at database for restricted collection as user") {
    val mapToInsert: List[Map[String, Any]] = (0 to 10)
      .map(i => Map("index" -> i, "hallo" -> "welt", "uuid" -> UUID.randomUUID().toString, "name" -> Random.alphanumeric.take(10).mkString))
      .toList

    val resultFuture   = TestAdditions.backend.send(api.insertMany("", testUserBearerToken)(Random.alphanumeric.take(10).mkString, mapToInsert))
    val responseResult = Await.result(resultFuture, 1.seconds)
    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for collection")
  }

}
