package dev.mongocamp.server.service

import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.service.CollectionNameService.CollectionName

class CollectionNameServiceSuite extends munit.FunSuite {

  private val prefix = "mc_"

  private def target(source: String, target: String) = CollectionNameService.resolveTarget(CollectionNameService.parse(source), target, "test", prefix)

  private def refused(body: => Any): Unit = {
    val exception = intercept[MongoCampException](body)
    assertEquals(exception.statusCode.code, 400)
    assertEquals(exception.errorCode, ErrorCodes.invalidCollectionName)
  }

  test("a collection of the default database and of another database") {
    assertEquals(CollectionNameService.parse("accounts"), CollectionName(None, "accounts"))
    assertEquals(CollectionNameService.parse("geodata:locations"), CollectionName(Some("geodata"), "locations"))
    assertEquals(CollectionNameService.parse("geodata:locations").full, "geodata:locations")
  }

  test("the target is in the database of the source") {
    assertEquals(target("accounts", "people"), CollectionName(None, "people"))
    assertEquals(target("geodata:locations", "places"), CollectionName(Some("geodata"), "places"))
    assertEquals(target("geodata:locations", "geodata:places").full, "geodata:places")
    assertEquals(target("accounts", "test:people"), CollectionName(None, "people"))
  }

  test("another database is refused") {
    refused(target("geodata:locations", "other:places"))
    refused(target("accounts", "other:people"))
  }

  test("names that are not allowed") {
    List("", "   ", "a$b", "x" * 121, "system.views", "mc_users", "docs.files", "docs.chunks", "a\u0000b").foreach(
      name => refused(target("accounts", name))
    )
    refused(target("accounts", "accounts"))
    refused(target("geodata:locations", "geodata:locations"))
  }

  test("the names with a dot and a dash are fine") {
    assertEquals(target("accounts", "my-data.2024").name, "my-data.2024")
  }

  test("sources that belong to the server or to a bucket are refused") {
    List("mc_users", "system.views", "sample-files.files", "sample-files.chunks", "geodata:mc_jobs").foreach(
      name => refused(CollectionNameService.checkSource(CollectionNameService.parse(name), prefix))
    )
    CollectionNameService.checkSource(CollectionNameService.parse("accounts"), prefix)
  }
}
