package dev.mongocamp.server.tests

import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.CollectionFieldsApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.MongoCampBaseServerSuite

// the fields of a collection as a flat list with the types of the values
class FieldDetailsSuite extends MongoCampBaseServerSuite {

  private lazy val fieldsApi     = CollectionFieldsApi()
  private lazy val documentsApi  = DocumentApi()
  private lazy val collectionApi = CollectionApi()

  private val collection = "fieldDetailsTest"

  private def details(sample: Option[Int] = None, depth: Option[Int] = None, name: String = collection, token: String = adminBearerToken) =
    executeRequest(fieldsApi.getFieldDetails("", "", token, "")(name, sample, depth))

  private def byPath(sample: Option[Int] = None, depth: Option[Int] = None) = {
    val response = details(sample, depth)
    assertEquals(response.code.code, 200, response.body.fold(_.getMessage, _ => ""))
    response.body
      .getOrElse(Nil)
      .map(
        f =>
          (
            f.path,
            f.types
              .map(
                t => (t.`type`, t.count)
              )
              .toMap
          )
      )
      .toMap
  }

  test("prepare") {
    val documents: Seq[Map[String, Any]] = (1 to 20).map(
      i => Map[String, Any]("name" -> s"doc$i", "age" -> (if (i <= 15) i else s"$i"), "address" -> Map("city" -> "c", "zip" -> i), "tags" -> List("a", "b"))
    )
    executeRequestToResponse(documentsApi.insertMany("", "", adminBearerToken, "")(collection, documents))
  }

  test("one entry per path, the elements of an array are path[]") {
    assertEquals(byPath().keySet, Set("_id", "name", "age", "address", "address.city", "address.zip", "tags", "tags[]"))
  }

  test("the types of a path with the number of the values of every type") {
    val fields = byPath()
    assertEquals(fields("name"), Map("string" -> 20L))
    assertEquals(fields("age"), Map("long" -> 15L, "string" -> 5L))
    assertEquals(fields("address"), Map("object" -> 20L))
    assertEquals(fields("address.zip"), Map("long" -> 20L))
    assertEquals(fields("tags"), Map("array" -> 20L))
    assertEquals(fields("tags[]"), Map("string" -> 40L))
  }

  test("the depth limits the nested paths") {
    val flat = byPath(depth = Some(0)).keySet
    assert(flat.contains("address"), flat.toString)
    assertEquals(flat.exists(_.startsWith("address.")), false, flat.toString)
  }

  test("the sample limits the documents that are analysed") {
    val fields = byPath(sample = Some(5))
    assertEquals(fields("name"), Map("string" -> 5L))
  }

  test("the share of the analysed documents is not rounded down to 0 for a sample") {
    val analysis = executeRequestToResponse(collectionApi.getSchemaAnalysis("", "", adminBearerToken, "")(collection, Some(5)))
    assertEquals(analysis.count, 20L)
    assertEquals(analysis.sample, 5L)
    assertEquals(analysis.percentageOfAnalysed, 0.25)
    val whole = executeRequestToResponse(collectionApi.getSchemaAnalysis("", "", adminBearerToken, "")(collection))
    assertEquals(whole.percentageOfAnalysed, 1.0)
  }

  test("a user without a read grant gets no fields, a user with a read grant does") {
    val denied = details(name = "other", token = testUserBearerToken).code.code
    assert(Set(401, 403).contains(denied), s"unexpected status $denied")
    assertEquals(details(name = "accounts", token = testUserBearerToken).code.code, 200)
  }

  test("cleanup") {
    executeRequestToResponse(collectionApi.deleteCollection("", "", adminBearerToken, "")(collection))
  }
}
