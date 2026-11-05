package dev.mongocamp.server.tests

import dev.mongocamp.server.test.client.api.CollectionApi
import dev.mongocamp.server.test.client.api.DocumentApi
import dev.mongocamp.server.test.client.api.IndexApi
import dev.mongocamp.server.test.client.api.IndexStatsApi
import dev.mongocamp.server.test.client.model.IndexCreateRequest
import dev.mongocamp.server.test.MongoCampBaseServerSuite

// the usage of the indices and the details of a collection without the address of the database server
class IndexStatsSuite extends MongoCampBaseServerSuite {

  private lazy val statsApi     = IndexStatsApi()
  private lazy val indexApi     = IndexApi()
  private lazy val documentsApi = DocumentApi()
  private lazy val colApi       = CollectionApi()

  private val collection = "indexStatsTest"

  test("prepare") {
    executeRequestToResponse(
      documentsApi.insertMany("", "", adminBearerToken, "")(
        collection,
        (1 to 20).map(
          i => Map("a" -> i, "b" -> s"v$i")
        )
      )
    )
    executeRequestToResponse(indexApi.createIndex("", "", adminBearerToken, "")(collection, IndexCreateRequest(Map("a" -> 1), None)))
    // use the index
    executeRequestToResponse(documentsApi.listDocuments("", "", adminBearerToken, "")(collection, Some("a:5"), Seq(), Seq(), Some(10), Some(1)))
  }

  test("the usage of the indices of a collection") {
    val usage = executeRequestToResponse(statsApi.indexStats("", "", adminBearerToken, "")(collection))
    assertEquals(usage.map(_.name).toSet, Set("_id_", "a_1"))
    assertEquals(usage.find(_.name == "a_1").map(_.key), Some(Map[String, Any]("a" -> 1)))
    assert(usage.forall(_.ops >= 0))
    assert(usage.forall(_.since != null))
  }

  test("the usage needs the administrate grant") {
    val code = executeRequest(statsApi.indexStats("", "", testUserBearerToken, "")(collection)).code.code
    assert(Set(401, 403).contains(code), s"unexpected status $code")
  }

  test("the details of a collection do not contain the host, the namespace and the time of the server") {
    val status = executeRequestToResponse(colApi.getCollectionInformation("", "", adminBearerToken, "")(collection, Some(true)))
    assertEquals(status.map.nonEmpty, true, "the details are empty")
    def keys(value: Any): Set[String] = value match {
      case m: Map[_, _] =>
        m.asInstanceOf[Map[String, Any]]
          .flatMap(
            e => keys(e._2) + e._1
          )
          .toSet
      case l: Iterable[_] => l.flatMap(keys).toSet
      case _              => Set()
    }
    val all = keys(status.map)
    assertEquals(all.intersect(Set("host", "ns", "localTime")), Set[String]())
  }

  test("cleanup") {
    executeRequestToResponse(colApi.deleteCollection("", "", adminBearerToken, "")(collection))
  }
}
