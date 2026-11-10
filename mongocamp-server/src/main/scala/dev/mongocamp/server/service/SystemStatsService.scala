package dev.mongocamp.server.service

import com.mongodb.MongoCommandException
import com.typesafe.scalalogging.LazyLogging
import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.model.ConnectionStats
import dev.mongocamp.server.model.OpCounters
import dev.mongocamp.server.model.ReplicaSetState
import dev.mongocamp.server.model.SystemStats
import org.bson.BsonDocument
import org.mongodb.scala.bson.collection.immutable.Document
import scala.jdk.CollectionConverters._

/** The numbers of the database server: `serverStatus` (uptime, connections, operation counters) and `replSetGetStatus` (replica set). Nothing that tells where the
  * database is (host names, addresses) is given.
  */
object SystemStatsService extends LazyLogging {

  /** The error code of a command that the database user is not allowed to run. */
  val UnauthorizedCode: Int = 13

  def systemStats(): SystemStats = {
    val admin = MongoDatabase.databaseProvider.database("admin")
    val (serverStatus, partial) =
      try (Some(admin.runCommand(Map("serverStatus" -> 1)).result()), false)
      catch {
        case e: MongoCommandException if e.getErrorCode == UnauthorizedCode =>
          logger.debug("the database user is not allowed to read the server status")
          (None, true)
      }
    // a server that is not part of a replica set answers with an error (76), a user without the role with 13: no replica set information then
    val replicaSet =
      try parseReplSetStatus(admin.runCommand(Map("replSetGetStatus" -> 1)).result())
      catch {
        case e: MongoCommandException =>
          logger.debug("no replica set information: code %s".format(e.getErrorCode))
          None
      }
    build(serverStatus, replicaSet, partial)
  }

  def build(serverStatus: Option[Document], replicaSet: Option[ReplicaSetState], partial: Boolean): SystemStats =
    SystemStats(
      serverStatus.flatMap(parseUptime),
      serverStatus.flatMap(parseConnections),
      serverStatus.flatMap(parseOpCounters),
      replicaSet,
      partial
    )

  private def section(document: Document, name: String): Option[BsonDocument] =
    document.get(name).filter(_.isDocument).map(_.asDocument())

  private def number(document: BsonDocument, key: String): Long =
    Option(document.get(key)).filter(_.isNumber).map(_.asNumber().longValue()).getOrElse(0L)

  def parseUptime(document: Document): Option[Long] =
    document.get("uptime").filter(_.isNumber).map(_.asNumber().longValue())

  def parseConnections(document: Document): Option[ConnectionStats] =
    section(document, "connections").map(
      connections => ConnectionStats(number(connections, "current"), number(connections, "available"), number(connections, "totalCreated"))
    )

  def parseOpCounters(document: Document): Option[OpCounters] =
    section(document, "opcounters").map(
      counters =>
        OpCounters(
          number(counters, "insert"),
          number(counters, "query"),
          number(counters, "update"),
          number(counters, "delete"),
          number(counters, "getmore"),
          number(counters, "command")
        )
    )

  /** The state of the member that answered, the number of members and the healthy ones: no names of the members. */
  def parseReplSetStatus(document: Document): Option[ReplicaSetState] =
    for {
      name <- document.get("set").filter(_.isString).map(_.asString().getValue)
      members = document.get("members").filter(_.isArray).map(_.asArray().getValues.asScala.toList.filter(_.isDocument).map(_.asDocument())).getOrElse(Nil)
    } yield {
      val self = members.find(
        member => Option(member.get("self")).exists(value => value.isBoolean && value.asBoolean().getValue)
      )
      val state = self.flatMap(
        member => Option(member.get("stateStr")).filter(_.isString).map(_.asString().getValue)
      )
      val healthy = members.count(
        member => Option(member.get("health")).exists(value => value.isNumber && value.asNumber().intValue() == 1)
      )
      ReplicaSetState(name, state.getOrElse("UNKNOWN"), members.size, healthy)
    }
}
