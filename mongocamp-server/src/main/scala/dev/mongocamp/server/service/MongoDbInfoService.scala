package dev.mongocamp.server.service

import dev.mongocamp.driver.mongodb._
import dev.mongocamp.server.database.MongoDatabase
import dev.mongocamp.server.model.SystemInfo
import org.mongodb.scala.bson.collection.immutable.Document
import scala.jdk.CollectionConverters._

/** Keeps the answer of `read` for `durationMillis`. An answer is only kept when `read` succeeded: a failure is thrown to the caller and the next call asks
  * again. The clock is a parameter for the tests.
  */
class MongoDbInfoCache(read: () => SystemInfo, durationMillis: Long, clock: () => Long = () => System.currentTimeMillis()) {

  @volatile private var cached: Option[(Long, SystemInfo)] = None

  def get(): SystemInfo = {
    val now = clock()
    cached.filter(
      entry => now - entry._1 < durationMillis
    ) match {
      case Some(entry) => entry._2
      case None =>
        val info = read()
        cached = Some((now, info))
        info
    }
  }

  def clear(): Unit = cached = None
}

/** The version of the MongoDB server, read with the `buildInfo` command (no special privileges needed). The version changes only with an upgrade of the
  * database, so the answer is kept for an hour.
  */
object MongoDbInfoService {

  val CacheDurationMillis: Long = 60L * 60L * 1000L

  /** The oldest major version of MongoDB that is tested and supported (the CI runs 6.0 and newer). */
  val MinimumSupportedMongoDbMajor: Int = 6

  /** The text of the warning for a version that is older than the supported minimum, none for a supported or an unknown version. */
  def unsupportedVersionWarning(info: SystemInfo): Option[String] =
    info.mongoDbVersionArray.headOption
      .filter(
        major => major < MinimumSupportedMongoDbMajor
      )
      .map(
        _ =>
          s"MongoDB ${info.mongoDbVersion} is not supported, MongoDB $MinimumSupportedMongoDbMajor.0 and later is. The server may work, problems with this version are not fixed."
      )

  private val cache = new MongoDbInfoCache(() => readFromDatabase(), CacheDurationMillis)

  def systemInfo(): SystemInfo = cache.get()

  /** Forgets the cached answer, the next call asks the database again. */
  def clearCache(): Unit = cache.clear()

  def parse(document: Document): SystemInfo = {
    val version = document.get("version").filter(_.isString).map(_.asString().getValue).getOrElse("")
    val numbers = document
      .get("versionArray")
      .filter(_.isArray)
      .map(
        array =>
          array
            .asArray()
            .getValues
            .asScala
            .toList
            .filter(_.isNumber)
            .map(
              value => value.asNumber().intValue()
            )
      )
      .getOrElse(Nil)
    SystemInfo(version, numbers)
  }

  private def readFromDatabase(): SystemInfo =
    MongoDatabase.databaseProvider
      .database()
      .runCommand(Map("buildInfo" -> 1))
      .map(
        document => parse(document)
      )
      .result()
}
