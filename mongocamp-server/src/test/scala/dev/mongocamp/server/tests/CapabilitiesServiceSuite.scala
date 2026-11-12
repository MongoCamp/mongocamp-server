package dev.mongocamp.server.tests

import dev.mongocamp.server.service.CapabilitiesService

/** Needs no server and no database. */
class CapabilitiesServiceSuite extends munit.FunSuite {

  test("a feature is named once") {
    val duplicates = CapabilitiesService.Features.groupBy(identity).filter(_._2.size > 1).keys.toList
    assertEquals(duplicates, Nil)
  }

  test("the features of the server are named") {
    List("permissions", "count", "export", "import", "collectionCopy", "collectionRename", "systemInfo", "systemStats", "capabilities").foreach(
      feature => assert(CapabilitiesService.Features.contains(feature), s"$feature is missing")
    )
  }
}
