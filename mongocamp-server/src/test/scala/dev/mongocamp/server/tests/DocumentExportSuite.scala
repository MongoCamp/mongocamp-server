package dev.mongocamp.server.tests

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import dev.mongocamp.server.test.TestServer
import io.circe.parser.parse
import io.circe.Json
import org.mongodb.scala.bson.collection.immutable.Document
import sttp.client3._
import sttp.model.Uri

// the export of the documents of a collection as a stream: ndjson, json and csv
class DocumentExportSuite extends MongoCampBaseServerSuite {

  private val collection = "exportTest"
  private val big        = "exportBig"

  private def provider = MongoDatabase.databaseProvider

  private def request(name: String, query: String, token: String) = {
    val uri      = Uri.unsafeParse(TestServer.serverBaseUrl + "/mongodb/collections/" + name + "/documents/export" + (if (query.isEmpty) "" else "?" + query))
    val response = TestAdditions.backend.send(basicRequest.get(uri).header("Authorization", "Bearer " + token))
    (response.code.code, response.body.fold(identity, identity), response)
  }

  private def download(query: String, name: String = collection, token: String = adminBearerToken) = request(name, query, token)

  private def lines(text: String) = text.split("\n", -1).toList.filter(_.nonEmpty)

  test("prepare") {
    val dao = provider.dao(collection)
    dao.insertMany((1 to 1000).map(i => Document("i" -> i, "name" -> s"doc$i", "group" -> (if (i % 4 == 0) "a" else "b"), "when" -> new java.util.Date(1718452800000L)))).result()
    dao.insertMany(
      Seq(
        Document("i" -> 1001, "name" -> "with, comma", "group" -> "c"),
        Document("i" -> 1002, "name" -> "with \"quote\"", "group" -> "c"),
        Document("i" -> 1003, "name" -> "two\nlines", "group" -> "c", "nested" -> Document("x" -> 1))
      )
    ).result()
  }

  test("ndjson is the default: one document per line, with a file name for the download") {
    val (code, body, response) = download("")
    assertEquals(code, 200)
    assertEquals(response.header("Content-Disposition"), Some("attachment; filename=\"exportTest.ndjson\""))
    val documents = lines(body).map(line => parse(line).getOrElse(fail(s"not JSON: $line")))
    assertEquals(documents.size, 1003)
    assertEquals(documents.map(_.hcursor.get[Int]("i").toOption.get).toSet, (1 to 1003).toSet)
  }

  test("json is one array") {
    val (code, body, response) = download("format=json")
    assertEquals(code, 200)
    assertEquals(response.header("Content-Disposition"), Some("attachment; filename=\"exportTest.json\""))
    assertEquals(parse(body).toOption.flatMap(_.asArray).map(_.size), Some(1003))
  }

  test("csv has the columns of the projection in their order, a text with a comma, a quote or a line break is in quotes") {
    val (code, body, response) = download("format=csv&projection=i&projection=name&projection=nested&filter=group:c&sort=i")
    assertEquals(code, 200)
    assertEquals(response.header("Content-Disposition"), Some("attachment; filename=\"exportTest.csv\""))
    assertEquals(body, "i,name,nested\r\n1001,\"with, comma\",\r\n1002,\"with \"\"quote\"\"\",\r\n1003,\"two\nlines\",\"{\"\"x\"\":1}\"\r\n")
  }

  test("csv without a projection has the fields of the documents, the id first") {
    val (code, body, _) = download("format=csv&filter=group:a&limit=2&sort=i")
    assertEquals(code, 200)
    val header = body.split("\r\n").head.split(",").toList
    assertEquals(header.head, "_id")
    assertEquals(header.toSet, Set("_id", "i", "name", "group", "when", "nested"))
    assertEquals(body.split("\r\n").length, 3)
  }

  test("a filter, the sorting and the limit work like in the list") {
    val (_, filtered, _) = download("filter=group:a")
    assertEquals(lines(filtered).size, 250)
    val (_, sorted, _) = download("sort=-i&limit=3")
    assertEquals(lines(sorted).map(line => parse(line).toOption.get.hcursor.get[Int]("i").toOption.get), List(1003, 1002, 1001))
    val (_, projected, _) = download("projection=name&limit=1&sort=i")
    assertEquals(parse(lines(projected).head).toOption.get.asObject.map(_.keys.toSet), Some(Set("_id", "name")))
  }

  test("with extendedJson the types of the values are explicit, canonical can be written back as it is") {
    val (code, body, _) = download("format=ndjson&extendedJson=canonical&filter=name:doc7&limit=1")
    assertEquals(code, 200)
    val document = parse(lines(body).head).toOption.get
    assertEquals(document.hcursor.downField("i").get[String]("$numberInt"), Right("7"))
    assertEquals(document.hcursor.downField("_id").get[String]("$oid").isRight, true)
    assertEquals(document.hcursor.downField("when").downField("$date").get[String]("$numberLong"), Right("1718452800000"))
  }

  test("a bad format, filter, limit or value of extendedJson is a bad request") {
    assertEquals(download("format=xml")._1, 400)
    assertEquals(download("filter=name:(")._1, 400)
    assertEquals(download("limit=0")._1, 400)
    assertEquals(download("extendedJson=x")._1, 400)
  }

  test("an empty collection gives an empty answer in every format") {
    provider.dao("exportEmpty").insertOne(Document("a" -> 1)).result()
    provider.dao("exportEmpty").deleteMany(Document()).result()
    assertEquals(download("format=ndjson", name = "exportEmpty")._2, "")
    assertEquals(download("format=json", name = "exportEmpty")._2, "[]")
    assertEquals(download("format=csv&projection=a", name = "exportEmpty")._2, "a\r\n")
    provider.dao("exportEmpty").drop().result()
  }

  test("a big collection is exported completely") {
    val dao = provider.dao(big)
    (0 until 20).foreach(
      part => dao.insertMany((1 to 1000).map(i => Document("n" -> (part * 1000 + i), "text" -> ("x" * 200)))).result()
    )
    val (code, body, _) = download("", name = big)
    assertEquals(code, 200)
    assertEquals(lines(body).size, 20000)
    assertEquals(lines(download("format=csv&projection=n", name = big)._2).size, 20001)
  }

  test("the read grant is needed") {
    val denied = download("", name = "pokemon", token = testUserBearerToken)._1
    assert(Set(401, 403).contains(denied), s"unexpected status $denied")
    assertEquals(download("limit=1", name = "accounts", token = testUserBearerToken)._1, 200)
    assertEquals(download("", name = "exportMissing")._2, "")
  }

  test("cleanup") {
    provider.dao(collection).drop().result()
    provider.dao(big).drop().result()
  }
}
