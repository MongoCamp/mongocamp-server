package dev.mongocamp.server.tests

import dev.mongocamp.server.model.SystemInfo
import dev.mongocamp.server.service.MongoDbInfoCache
import dev.mongocamp.server.service.MongoDbInfoService
import org.mongodb.scala.bson.collection.immutable.Document

/** Needs no server and no database: the pure parts of the version information. */
class MongoDbInfoServiceSuite extends munit.FunSuite {

  private val seven = SystemInfo("7.0.5", List(7, 0, 5, 0))
  private val eight = SystemInfo("8.0.1", List(8, 0, 1, 0))

  test("parse the answer of buildInfo") {
    assertEquals(MongoDbInfoService.parse(Document("version" -> "7.0.5", "versionArray" -> Seq(7, 0, 5, 0))), seven)
  }

  test("parse a release candidate with a negative number") {
    assertEquals(MongoDbInfoService.parse(Document("version" -> "8.0.0-rc1", "versionArray" -> Seq(8, 0, 0, -37))), SystemInfo("8.0.0-rc1", List(8, 0, 0, -37)))
  }

  test("parse an answer without a version") {
    assertEquals(MongoDbInfoService.parse(Document("ok" -> 1)), SystemInfo("", Nil))
  }

  test("a version older than 6.0 gets a warning with the version in it") {
    List(SystemInfo("5.0.28", List(5, 0, 28, 0)), SystemInfo("4.4.29", List(4, 4, 29, 0))).foreach(
      info => {
        val warning = MongoDbInfoService.unsupportedVersionWarning(info)
        assert(warning.exists(_.contains(info.mongoDbVersion)), s"no warning for ${info.mongoDbVersion}")
        assert(warning.exists(_.contains("6.0")), "the minimum is missing in the warning")
      }
    )
  }

  test("a supported or an unknown version gets no warning") {
    List(SystemInfo("6.0.0", List(6, 0, 0, 0)), seven, eight, SystemInfo("", Nil)).foreach(
      info => assertEquals(MongoDbInfoService.unsupportedVersionWarning(info), None)
    )
  }

  test("the cache reads once within its duration") {
    var reads = 0
    var now   = 1000L
    val cache = new MongoDbInfoCache(
      () => { reads += 1; seven },
      60000L,
      () => now
    )
    assertEquals(cache.get(), seven)
    now += 59999L
    assertEquals(cache.get(), seven)
    assertEquals(reads, 1)
  }

  test("the cache reads again when its duration is over") {
    var reads = 0
    var now   = 1000L
    var info  = seven
    val cache = new MongoDbInfoCache(
      () => { reads += 1; info },
      60000L,
      () => now
    )
    assertEquals(cache.get(), seven)
    info = eight
    now += 60000L
    assertEquals(cache.get(), eight)
    assertEquals(reads, 2)
  }

  test("clear makes the next call read again") {
    var reads = 0
    val cache = new MongoDbInfoCache(
      () => { reads += 1; seven },
      60000L
    )
    cache.get()
    cache.clear()
    cache.get()
    assertEquals(reads, 2)
  }

  test("a failed read is not kept") {
    var reads = 0
    val cache = new MongoDbInfoCache(
      () => {
        reads += 1
        if (reads == 1) throw new IllegalStateException("database is gone") else seven
      },
      60000L
    )
    intercept[IllegalStateException](cache.get())
    assertEquals(cache.get(), seven)
    assertEquals(reads, 2)
  }
}
