package dev.mongocamp.server.tests

import better.files.File
import dev.mongocamp.driver.mongodb.GenericObservable
import dev.mongocamp.server.client.api.FileApi
import dev.mongocamp.server.client.model.{MongoFindRequest, UpdateFileInformationRequest}
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.model.BucketInformation.BucketCollectionSuffix
import dev.mongocamp.server.server.TestAdditions
import io.circe.syntax.EncoderOps

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Random

class FileSuite extends BaseSuite {

  val api: FileApi = FileApi()

  var fileId: String = ""

  private val BucketNameSample = "sample-files"

  test("list all files as admin") {
    val response = executeRequestToResponse(api.listFiles("", "", adminBearerToken, "")(BucketNameSample))
    assertEquals(response.size, 4)
    assertEquals(response.map(_.filename), List("accounts.json", "geodata.json", "users.json", "mongocamp.png"))
  }

  test("paginated files with filter as admin") {
    val responseResult = executeRequest(
      api.listFiles("", "", adminBearerToken, "")(
        BucketNameSample,
        Some(Map("filename" -> Map("$regex" -> "(.*?).json")).asJson.toString()),
        rowsPerPage = Some(2),
        page = Some(2)
      )
    )
    val response = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 1)
    assertEquals(responseResult.header("x-pagination-rows-per-page"), Some("2"))
    assertEquals(responseResult.header("x-pagination-current-page"), Some("2"))
    assertEquals(responseResult.header("x-pagination-count-rows"), Some("3"))
    assertEquals(responseResult.header("x-pagination-count-pages"), Some("2"))
    assertEquals(response.map(_.filename), List("users.json"))
  }

  test("list all files (post) as admin") {
    val response = executeRequestToResponse(api.findFiles("", "", adminBearerToken, "")(BucketNameSample, MongoFindRequest(Map(), Map(), Map())))
    assertEquals(response.size, 4)
    assertEquals(response.map(_.filename), List("accounts.json", "geodata.json", "users.json", "mongocamp.png"))
  }

  test("paginated files (post) with filter as admin") {
    val responseResult = executeRequest(
      api.findFiles("", "", adminBearerToken, "")(
        BucketNameSample,
        MongoFindRequest(Map("filename" -> Map("$regex" -> "(.*?).json")), Map(), Map()),
        rowsPerPage = Some(2),
        page = Some(2)
      )
    )
    val response = responseResult.body.getOrElse(throw new Exception("error"))
    assertEquals(response.size, 1)
    assertEquals(responseResult.header("x-pagination-rows-per-page"), Some("2"))
    assertEquals(responseResult.header("x-pagination-current-page"), Some("2"))
    assertEquals(responseResult.header("x-pagination-count-rows"), Some("3"))
    assertEquals(responseResult.header("x-pagination-count-pages"), Some("2"))
    assertEquals(response.map(_.filename), List("users.json"))
  }

  test("upload a new file as admin") {
    val geoFile = File(getClass.getResource("/geodata.json").getPath)
    val response =
      executeRequestToResponse(
        api.insertFile("", "", adminBearerToken, "")(
          BucketNameSample,
          geoFile.toJava,
          Map("originalFilePath" -> geoFile.pathAsString).asJson.toString(),
          Some("myFileName.txt")
        )
      )
    assertEquals(response.insertedIds.size, 1)
    fileId = response.insertedIds.head
    assertEquals(response.wasAcknowledged, true)
  }

  test("get file information of fileId as admin") {
    val geoFile = File(getClass.getResource("/geodata.json").getPath)

    val response =
      executeRequestToResponse(
        api.getFileInformation("", "", adminBearerToken, "")(BucketNameSample, fileId)
      )
    assertEquals(response._id, fileId)
    assertEquals(response.filename, "myFileName.txt")
    assertEquals(response.metadata, Map("originalFilePath" -> geoFile.pathAsString))
  }

  test("get file of fileId as admin") {
    val geoFile = File(getClass.getResource("/geodata.json").getPath)

    val request        = api.getFile("", "", adminBearerToken, "")(BucketNameSample, fileId, File.newTemporaryFile().toJava)
    val resultFuture   = TestAdditions.backend.send(request)
    val responseResult = Await.result(resultFuture, 60.seconds)
    val response: File = File(responseResult.body.getOrElse(throw new Exception("error")).getPath)

    assertEquals(response.size(), geoFile.size())
    assertEquals(response.sha512, geoFile.sha512)
  }

  test("update file information just filename of fileId as admin") {
    val geoFile     = File(getClass.getResource("/geodata.json").getPath)
    val newFileName = "myNewFileName.json"
    val response =
      executeRequestToResponse(api.updateFileInformation("", "", adminBearerToken, "")(BucketNameSample, fileId, UpdateFileInformationRequest(Some(newFileName), None)))
    assertEquals(response.matchedCount, 1L)
    assertEquals(response.modifiedCount, 1L)
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.upsertedIds, List(fileId))
    val validationResponse = executeRequestToResponse(api.getFileInformation("", "", adminBearerToken, "")(BucketNameSample, fileId))
    assertEquals(validationResponse._id, fileId)
    assertEquals(validationResponse.filename, newFileName)
    assertEquals(validationResponse.metadata, Map("originalFilePath" -> geoFile.pathAsString))
//    assertEquals(response.metadata, Map("originalFilePath" -> geoFile.pathAsString))
  }

  test("update file information just metadata of fileId as admin") {
    val response =
      executeRequestToResponse(
        api.updateFileInformation("", "", adminBearerToken, "")(BucketNameSample, fileId, UpdateFileInformationRequest(None, Some(Map("new" -> "value"))))
      )
    assertEquals(response.matchedCount, 1L)
    assertEquals(response.modifiedCount, 1L)
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.upsertedIds, List(fileId))
    val validationResponse = executeRequestToResponse(api.getFileInformation("", "", adminBearerToken, "")(BucketNameSample, fileId))
    assertEquals(validationResponse._id, fileId)
    assertEquals(validationResponse.filename, "myNewFileName.json")
    assertEquals(validationResponse.metadata, Map("new" -> "value"))
  }

  test("update file information metadata and filename of fileId as admin") {
    val newFileName = Random.alphanumeric.take(10).mkString + ".json"
    val response =
      executeRequestToResponse(
        api.updateFileInformation("", "", adminBearerToken, "")(
          BucketNameSample,
          fileId,
          UpdateFileInformationRequest(Some(newFileName), Some(Map("new" -> Map("crazy" -> "value"))))
        )
      )
    assertEquals(response.matchedCount, 1L)
    assertEquals(response.modifiedCount, 1L)
    assertEquals(response.wasAcknowledged, true)
    assertEquals(response.upsertedIds, List(fileId))
    val validationResponse = executeRequestToResponse(api.getFileInformation("", "", adminBearerToken, "")(BucketNameSample, fileId))
    assertEquals(validationResponse._id, fileId)
    assertEquals(validationResponse.filename, newFileName)
    assertEquals(validationResponse.metadata, Map("new" -> Map("crazy" -> "value")))
  }

  test("delete file of fileId as admin") {
    val countStart = MongoDatabase.databaseProvider.dao(s"$BucketNameSample$BucketCollectionSuffix").count().result()
    val response =
      executeRequestToResponse(
        api.deleteFile("", "", adminBearerToken, "")(BucketNameSample, fileId)
      )
    assertEquals(response.deletedCount, 1L)
    assertEquals(response.wasAcknowledged, true)
    val countEnd = MongoDatabase.databaseProvider.dao(s"$BucketNameSample$BucketCollectionSuffix").count().result()
    assertEquals(countStart - 1, countEnd)
    assertEquals(countEnd, 4L)
  }

  test("list all files as user") {
    val response = executeRequest(api.listFiles("", "", testUserBearerToken, "")(BucketNameSample))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for bucket")
  }

  test("paginated files with filter as user") {
    val response = executeRequest(
      api.listFiles("", "", testUserBearerToken, "")(
        BucketNameSample,
        Some(Map("filename" -> Map("$regex" -> "(.*?).json")).asJson.toString()),
        rowsPerPage = Some(2),
        page = Some(2)
      )
    )
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for bucket")
  }

  test("list all files (post) as user") {
    val response = executeRequest(api.findFiles("", "", testUserBearerToken, "")(BucketNameSample, MongoFindRequest(Map(), Map(), Map())))
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for bucket")
  }

  test("paginated files (post) with filter as user") {
    val response = executeRequest(
      api.findFiles("", "", testUserBearerToken, "")(
        BucketNameSample,
        MongoFindRequest(Map("filename" -> Map("$regex" -> "(.*?).json")), Map(), Map()),
        rowsPerPage = Some(2),
        page = Some(2)
      )
    )
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for bucket")
  }

  test("upload a new file as user") {
    val geoFile = File(getClass.getResource("/geodata.json").getPath)
    val response =
      executeRequest(
        api.insertFile("", "", testUserBearerToken, "")(
          BucketNameSample,
          geoFile.toJava,
          Map("originalFilePath" -> geoFile.pathAsString).asJson.toString(),
          Some("myFileName.txt")
        )
      )
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for bucket")
  }

  test("get file information of fileId as user") {
    val geoFile = File(getClass.getResource("/geodata.json").getPath)

    val response =
      executeRequest(
        api.getFileInformation("", "", testUserBearerToken, "")(BucketNameSample, fileId)
      )
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for bucket")
  }

  test("get file of fileId as user") {
    val geoFile = File(getClass.getResource("/geodata.json").getPath)

    val request        = api.getFile("", "", testUserBearerToken, "")(BucketNameSample, fileId, File.newTemporaryFile().toJava)
    val resultFuture   = TestAdditions.backend.send(request)
    val responseResult = Await.result(resultFuture, 60.seconds)

    assertEquals(responseResult.code.code, 401)
    assertEquals(responseResult.header("x-error-message").isDefined, true)
    assertEquals(responseResult.header("x-error-message").get, "user not authorized for bucket")

  }

  test("delete file of fileId as user") {
    val response =
      executeRequest(
        api.deleteFile("", "", testUserBearerToken, "")(BucketNameSample, fileId)
      )
    assertEquals(response.code.code, 401)
    assertEquals(response.header("x-error-message").isDefined, true)
    assertEquals(response.header("x-error-message").get, "user not authorized for bucket")
  }

}
