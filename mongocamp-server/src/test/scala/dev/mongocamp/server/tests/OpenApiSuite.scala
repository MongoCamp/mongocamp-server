package dev.mongocamp.server.tests

import dev.mongocamp.server.database.TestAdditions
import dev.mongocamp.server.test.MongoCampBaseServerSuite
import dev.mongocamp.server.test.TestServer
import sttp.client3._

// generated clients type the values by this document: free-form JSON must not be described as string (#71)
class OpenApiSuite extends MongoCampBaseServerSuite {

  private lazy val yaml: String = {
    val response = basicRequest.get(uri"${TestServer.serverBaseUrl}/docs/docs.yaml").send(TestAdditions.backend)
    assertEquals(response.code.code, 200)
    response.body.getOrElse("")
  }

  // the lines of a block of the yaml: from the line `start` to the next line with the same or a smaller indentation
  private def block(start: String): List[String] = {
    val lines  = yaml.linesIterator.toList
    val index  = lines.indexWhere(_ == start)
    assert(index >= 0, s"`$start` not found in the OpenAPI document")
    val indent = start.takeWhile(_ == ' ').length
    lines.drop(index + 1).takeWhile(line => line.isBlank || line.takeWhile(_ == ' ').length > indent)
  }

  test("the value of a pipeline stage is any JSON value") {
    val stage = block("    PipelineStage:").map(_.trim)
    assertEquals(stage.contains("value: {}"), true, stage.mkString("\n"))
  }

  test("the bodies of documents and filters are objects with any values") {
    val map = block("    Map_Any:").map(_.trim)
    assertEquals(map.contains("additionalProperties: {}"), true, map.mkString("\n"))
  }

  test("the response of distinct has items of any type") {
    val distinct = block("  /mongodb/collections/{collectionName}/distinct/{field}:").map(_.trim)
    assertEquals(distinct.contains("items: {}"), true, "distinct items are not free-form")
  }
}
