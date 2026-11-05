package dev.mongocamp.server.converter

import dev.mongocamp.server.converter.LuceneQueryTools.InvalidFilterException

class LuceneQueryToolsSuite extends munit.FunSuite {

  test("without a query the filter is empty") {
    assertEquals(LuceneQueryTools.toFilterMap(None), Map[String, Any]())
  }

  test("a filter string becomes a map for the database") {
    assertEquals(LuceneQueryTools.toFilterMap(Some(LuceneQueryTools.parseFilter("name:John"))), Map[String, Any]("name" -> Map("$eq" -> "John")))
    val range = LuceneQueryTools.toFilterMap(Some(LuceneQueryTools.parseFilter("age:[18 TO 65]")))
    assertEquals(range.keySet, Set("age"))
  }

  test("a quoted value with special characters stays one value") {
    assertEquals(LuceneQueryTools.toFilterMap(Some(LuceneQueryTools.parseFilter("city:\"x:y\""))), Map[String, Any]("city" -> Map("$eq" -> "x:y")))
  }

  test("a filter that cannot be parsed or converted is an invalid filter") {
    intercept[InvalidFilterException](LuceneQueryTools.parseFilter("name:(unclosed"))
  }

  test("the codec turns the failure into a decode error") {
    assert(LuceneQueryTools.filterCodec.decode("name:(unclosed").isInstanceOf[sttp.tapir.DecodeResult.Error])
    assert(LuceneQueryTools.filterCodec.decode("name:John").isInstanceOf[sttp.tapir.DecodeResult.Value[?]])
  }
}
