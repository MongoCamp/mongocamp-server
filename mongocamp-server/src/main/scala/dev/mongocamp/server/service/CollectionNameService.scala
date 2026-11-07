package dev.mongocamp.server.service

import dev.mongocamp.driver.mongodb.database.DatabaseProvider.CollectionSeparator
import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.BucketInformation.BucketCollectionSuffix
import dev.mongocamp.server.model.BucketInformation.GridFsBucketChunksSuffix

/** The names of collections for the routes that rename and copy a collection. A collection is `name` in the default database and `database:name` in another. */
object CollectionNameService {

  val MaxNameLength = 120

  case class CollectionName(database: Option[String], name: String) {
    def full: String = database
      .map(
        db => s"$db$CollectionSeparator$name"
      )
      .getOrElse(name)
  }

  private def invalid(message: String) = MongoCampException.badRequestException(message, ErrorCodes.invalidCollectionName)

  def parse(collection: String): CollectionName =
    if (collection.contains(CollectionSeparator)) {
      val index = collection.indexOf(CollectionSeparator)
      CollectionName(Some(collection.substring(0, index)), collection.substring(index + CollectionSeparator.length))
    }
    else CollectionName(None, collection)

  /** The names that belong to the server, to a bucket or to the database are not the business of a user: the collections of a bucket have their own routes. */
  def isReserved(name: String, systemPrefix: String): Boolean =
    name.startsWith("system.") || name.endsWith(BucketCollectionSuffix) || name.endsWith(GridFsBucketChunksSuffix) || (systemPrefix.nonEmpty && name.startsWith(
      systemPrefix
    ))

  def checkSource(source: CollectionName, systemPrefix: String): Unit =
    if (isReserved(source.name, systemPrefix)) {
      throw invalid(s"the collection ${source.name} belongs to the server or to a bucket and cannot be renamed or copied here")
    }

  /** The target in the database of the source. A target with another database is refused: the database cannot rename or copy to another database. */
  def resolveTarget(source: CollectionName, target: String, defaultDatabase: String, systemPrefix: String): CollectionName = {
    val parsed         = parse(target.trim)
    val sourceDatabase = source.database.getOrElse(defaultDatabase)
    parsed.database.foreach(
      database =>
        if (!database.equalsIgnoreCase(sourceDatabase)) {
          throw invalid(s"the target is in the database $database, the collection is in $sourceDatabase")
        }
    )
    val name = parsed.name
    if (name.isBlank) throw invalid("the name of the target is empty")
    if (name.length > MaxNameLength) throw invalid(s"the name of the target is longer than $MaxNameLength characters")
    if (
      name.exists(
        c => c == '$' || c == '\u0000' || c.isControl
      )
    ) throw invalid("the name of the target contains characters that are not allowed ($, control characters)")
    if (isReserved(name, systemPrefix))
      throw invalid(s"the name $name is reserved (system collections, the prefix of the server and the collections of a bucket)")
    val resolved = CollectionName(source.database, name)
    if (resolved.name == source.name) throw invalid("the target is the collection itself")
    resolved
  }
}
