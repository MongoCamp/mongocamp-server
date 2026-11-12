package dev.mongocamp.server.tests

import better.files.File
import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.database.TestAdditions.copyResourceFileToTempDir
import dev.mongocamp.server.test.client.api.BucketApi
import dev.mongocamp.server.test.client.api.DatabaseApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.client.api.FileApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import io.circe.syntax.EncoderOps

// plan 05 phase 10: collections and buckets of a database other than the default one, as admin and as a user with grants
class OtherDatabaseSuite extends MongoCampBaseServerSuite {

  private lazy val documentApi = DocumentApi()
  private lazy val databaseApi = DatabaseApi()
  private lazy val bucketApi   = BucketApi()
  private lazy val fileApi     = FileApi()

  private def read(token: String, collection: String) =
    executeRequest(documentApi.listDocuments("", "", token, "")(collection, None, Seq(), Seq(), Some(10), Some(1)))

  private def write(token: String, collection: String) =
    executeRequest(documentApi.insert("", "", token, "")(collection, Map("name" -> "grantTest")))

  // the grant of the test user is `geodata:locations` (read only): a grant is the name `database:collection`, as in the path
  test("a user reads the collection of another database that a grant names") {
    assertEquals(read(testUserBearerToken, "geodata:locations").code.code, 200)
  }

  test("a read grant is no write grant") {
    assertEquals(write(testUserBearerToken, "geodata:locations").code.code, 401)
  }

  test("another collection of that database and the same name in the default database are denied") {
    assertEquals(read(testUserBearerToken, "geodata:companies").code.code, 401)
    assertEquals(read(testUserBearerToken, "locations").code.code, 401)
    assertEquals(write(testUserBearerToken, "geodata:companies").code.code, 401)
  }

  test("a grant of the default database does not open a collection of the same name in another database") {
    assertEquals(read(testUserBearerToken, "accounts").code.code, 200)
    assertEquals(read(testUserBearerToken, "otherdb:accounts").code.code, 401)
  }

  test("the list of the collections of a database shows only what the user may read") {
    val names = executeRequestToResponse(databaseApi.listCollectionsByDatabase("", "", testUserBearerToken, "")("geodata"))
    assertEquals(names, List("locations"))
    val adminNames = executeRequestToResponse(databaseApi.listCollectionsByDatabase("", "", adminBearerToken, "")("geodata"))
    assertEquals(adminNames.contains("companies"), true)
  }

  private val bucket = "otherdb:phase10"

  test("a bucket of another database: upload, read, info, delete the file") {
    val geoFile: File = copyResourceFileToTempDir(TestAdditions.tempDir, "geodata.json")
    val inserted = executeRequest(fileApi.insertFile("", "", adminBearerToken, "")(bucket, geoFile.toJava, Map("a" -> "b").asJson.toString(), Some("geo.json")))
    assertEquals(inserted.code.code, 200, inserted.body.fold(_.getMessage, _ => ""))
    val fileId = inserted.body.toOption.get.insertedIds.head

    val files = executeRequestToResponse(fileApi.listFiles("", "", adminBearerToken, "")(bucket))
    assertEquals(files.map(_.filename), List("geo.json"))

    val downloaded = TestAdditions.backend.send(fileApi.getFile("", "", adminBearerToken, "")(bucket, fileId, File.newTemporaryFile().toJava))
    assertEquals(File(downloaded.body.getOrElse(throw new Exception("download failed")).getPath).size(), geoFile.size())

    val info = executeRequestToResponse(bucketApi.getBucket("", "", adminBearerToken, "")(bucket))
    assertEquals(info.files, 1L)

    // the default database has no such bucket and its list is unchanged
    assertEquals(executeRequestToResponse(bucketApi.listBuckets("", "", adminBearerToken, "")()).contains("phase10"), false)

    executeRequestToResponse(fileApi.deleteFile("", "", adminBearerToken, "")(bucket, fileId))
    assertEquals(executeRequestToResponse(fileApi.listFiles("", "", adminBearerToken, "")(bucket)).size, 0)
  }

  test("a user without a bucket grant is denied on a bucket of another database") {
    val response = executeRequest(bucketApi.getBucket("", "", testUserBearerToken, "")(bucket))
    assertEquals(response.code.code, 401)
  }

  test("clear and delete the bucket of another database") {
    assertEquals(executeRequestToResponse(bucketApi.clearBucket("", "", adminBearerToken, "")(bucket)).value, true)
    assertEquals(executeRequestToResponse(bucketApi.deleteBucket("", "", adminBearerToken, "")(bucket)).value, true)
  }

  test("a bucket that does not exist answers with zeros, in the default and in another database") {
    List("missingBucket", "otherdb:missingBucket").foreach(
      name => {
        val response = executeRequest(bucketApi.getBucket("", "", adminBearerToken, "")(name))
        assertEquals(response.code.code, 200, name)
        val info = response.body.getOrElse(throw new Exception(name))
        assertEquals((info.name, info.files, info.size, info.avgObjectSize), (name, 0L, 0.0, 0.0))
      }
    )
  }
}
