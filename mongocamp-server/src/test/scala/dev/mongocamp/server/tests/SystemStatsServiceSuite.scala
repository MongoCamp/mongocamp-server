package dev.mongocamp.server.tests

import dev.mongocamp.server.model.ConnectionStats
import dev.mongocamp.server.model.OpCounters
import dev.mongocamp.server.model.ReplicaSetState
import dev.mongocamp.server.model.SystemStats
import dev.mongocamp.server.service.SystemStatsService
import org.mongodb.scala.bson.collection.immutable.Document

/** Needs no server and no database: the pure parts of the system statistics. */
class SystemStatsServiceSuite extends munit.FunSuite {

  // the host and the version are in the real answer, they must not get into the statistics
  private val serverStatus = Document(
    "host"        -> "db-1.internal:27017",
    "version"     -> "7.0.5",
    "uptime"      -> 1234.7,
    "connections" -> Document("current" -> 3, "available" -> 800, "totalCreated" -> 10L),
    "opcounters"  -> Document("insert" -> 1L, "query" -> 2L, "update" -> 3L, "delete" -> 4L, "getmore" -> 5L, "command" -> 6L)
  )

  test("the numbers of the server status") {
    val stats = SystemStatsService.build(Some(serverStatus), None, partial = false)
    assertEquals(stats, SystemStats(Some(1234L), Some(ConnectionStats(3, 800, 10)), Some(OpCounters(1, 2, 3, 4, 5, 6)), None, partial = false))
  }

  test("a section that is missing is not given") {
    val stats = SystemStatsService.build(Some(Document("uptime" -> 5)), None, partial = false)
    assertEquals(stats, SystemStats(Some(5L), None, None, None, partial = false))
  }

  test("a missing server status is partial") {
    assertEquals(SystemStatsService.build(None, None, partial = true), SystemStats(None, None, None, None, partial = true))
  }

  test("the state of a replica set without the names of the members") {
    val replSet = Document(
      "set"     -> "rs0",
      "myState" -> 1,
      "members" -> Seq(
        Document("name" -> "db-1.internal:27017", "health" -> 1.0, "stateStr" -> "PRIMARY", "self" -> true),
        Document("name" -> "db-2.internal:27017", "health" -> 1.0, "stateStr" -> "SECONDARY"),
        Document("name" -> "db-3.internal:27017", "health" -> 0.0, "stateStr" -> "(not reachable/healthy)")
      )
    )
    val state = SystemStatsService.parseReplSetStatus(replSet)
    assertEquals(state, Some(ReplicaSetState("rs0", "PRIMARY", 3, 2)))
    assert(!state.toString.contains("internal"), "a host name got into the state")
  }

  test("an answer that is not a replica set status gives no state") {
    assertEquals(SystemStatsService.parseReplSetStatus(Document("ok" -> 1)), None)
  }

  test("a member that answered is unknown when it is not marked") {
    val replSet = Document("set" -> "rs0", "members" -> Seq(Document("health" -> 1.0, "stateStr" -> "SECONDARY")))
    assertEquals(SystemStatsService.parseReplSetStatus(replSet), Some(ReplicaSetState("rs0", "UNKNOWN", 1, 1)))
  }
}
