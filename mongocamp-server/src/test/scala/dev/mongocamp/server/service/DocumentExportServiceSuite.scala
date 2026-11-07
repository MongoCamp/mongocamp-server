package dev.mongocamp.server.service

import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.service.DocumentExportService._
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.Sink
import org.apache.pekko.stream.scaladsl.Source
import scala.concurrent.duration._
import scala.concurrent.Await

class DocumentExportServiceSuite extends munit.FunSuite {

  private lazy val system = ActorSystem("export-test")

  override def afterAll(): Unit = Await.result(system.terminate(), 10.seconds)

  private def run(format: ExportFormat, documents: List[Map[String, Any]], columns: List[String] = List()): String =
    Await.result(render(format, Source(documents), columns).runWith(Sink.seq)(org.apache.pekko.stream.Materializer(system)), 10.seconds).mkString

  private val documents = List(Map[String, Any]("a" -> 1, "b" -> "x"), Map[String, Any]("a" -> 2, "b" -> "y"))

  test("the formats are found by name, another is a bad request") {
    assertEquals(parseFormat("csv"), Csv)
    assertEquals(parseFormat(" JSON "), JsonArray)
    assertEquals(parseFormat("ndjson"), Ndjson)
    assertEquals(Formats.map(_.extension), List("ndjson", "json", "csv"))
    assertEquals(intercept[MongoCampException](parseFormat("xml")).statusCode.code, 400)
  }

  test("ndjson has one document per line") {
    assertEquals(run(Ndjson, documents), "{\"a\":1,\"b\":\"x\"}\n{\"a\":2,\"b\":\"y\"}\n")
    assertEquals(run(Ndjson, List()), "")
  }

  test("json is one array, also without a document") {
    assertEquals(run(JsonArray, documents), "[{\"a\":1,\"b\":\"x\"},{\"a\":2,\"b\":\"y\"}]")
    assertEquals(run(JsonArray, List(documents.head)), "[{\"a\":1,\"b\":\"x\"}]")
    assertEquals(run(JsonArray, List()), "[]")
  }

  test("csv has the header and a line per document, the columns decide what is written") {
    assertEquals(run(Csv, documents, List("a", "b")), "a,b\r\n1,x\r\n2,y\r\n")
    assertEquals(run(Csv, documents, List("b", "missing", "a")), "b,missing,a\r\nx,,1\r\ny,,2\r\n")
    assertEquals(run(Csv, List(), List("a", "b")), "a,b\r\n")
  }

  test("a cell with a comma, a quote or a line break is in quotes") {
    assertEquals(csvCell("plain"), "plain")
    assertEquals(csvCell("a,b"), "\"a,b\"")
    assertEquals(csvCell("say \"hi\""), "\"say \"\"hi\"\"\"")
    assertEquals(csvCell("line1\nline2"), "\"line1\nline2\"")
    assertEquals(csvCell("line1\r\nline2"), "\"line1\r\nline2\"")
    assertEquals(csvCell(" spaces "), " spaces ")
  }

  test("a cell: null is empty, numbers and booleans are written, objects and lists are JSON") {
    assertEquals(csvCell(null), "")
    assertEquals(csvCell(42), "42")
    assertEquals(csvCell(1.5), "1.5")
    assertEquals(csvCell(true), "true")
    assertEquals(csvCell(Map("a" -> 1)), "\"{\"\"a\"\":1}\"")
    assertEquals(csvCell(List(1, 2)), "\"[1,2]\"")
    assertEquals(csvHeader(List("id", "full name", "a,b")), "id,full name,\"a,b\"\r\n")
  }
}
