package dev.mongocamp.server.service

import dev.mongocamp.driver.mongodb.bson.BsonConverter
import dev.mongocamp.server.model.index.IndexInfo
import dev.mongocamp.server.model.index.IndexKey
import org.bson.BsonDocument
import org.bson.BsonValue
import scala.jdk.CollectionConverters._

/** Builds the description of an index from the document of the database. The driver converts the keys to a Scala map, that loses the order of the keys of a
  * compound index, and a text index only has the internal keys `_fts` and `_ftsx`. Here the document is read as it is.
  */
object IndexInfoService {

  private val TextKey      = "_fts"
  private val TextOrderKey = "_ftsx"

  def indexInfo(document: BsonDocument): IndexInfo = {
    val weights = optionalDocument(document, "weights")
    IndexInfo(
      name = document.getString("name").getValue,
      keys = indexKeys(document.getDocument("key"), weights),
      unique = flag(document, "unique"),
      sparse = flag(document, "sparse"),
      hidden = flag(document, "hidden"),
      partialFilterExpression = optionalMap(document, "partialFilterExpression"),
      collation = optionalMap(document, "collation"),
      weights = weights.map(toMap),
      defaultLanguage = Option(document.get("default_language")).filter(_.isString).map(_.asString().getValue),
      expireAfterSeconds = Option(document.get("expireAfterSeconds")).filter(_.isNumber).map(_.asNumber().longValue()),
      version = Option(document.get("v")).filter(_.isNumber).map(_.asNumber().intValue()).getOrElse(0)
    )
  }

  def indexKeys(key: BsonDocument, weights: Option[BsonDocument]): List[IndexKey] =
    key.entrySet().asScala.toList.flatMap(
      entry =>
        entry.getKey match {
          case TextKey      => weights.map(_.keySet().asScala.toList.map(IndexKey(_, "text"))).getOrElse(List(IndexKey(TextKey, "text")))
          case TextOrderKey => Nil
          case field        => List(IndexKey(field, direction(entry.getValue)))
        }
    )

  private def direction(value: BsonValue): String =
    if (value.isNumber) {
      val number = value.asNumber().doubleValue()
      if (number == 1.0) "1" else if (number == -1.0) "-1" else value.asNumber().decimal128Value().toString
    }
    else if (value.isString) value.asString().getValue
    else value.toString

  private def flag(document: BsonDocument, name: String): Boolean = Option(document.get(name)).exists(_.isBoolean) && document.getBoolean(name).getValue

  private def optionalDocument(document: BsonDocument, name: String): Option[BsonDocument] = Option(document.get(name)).filter(_.isDocument).map(_.asDocument())

  private def optionalMap(document: BsonDocument, name: String): Option[Map[String, Any]] = optionalDocument(document, name).map(toMap)

  private def toMap(document: BsonDocument): Map[String, Any] =
    document.entrySet().asScala.map(entry => (entry.getKey, BsonConverter.fromBson(entry.getValue))).toMap
}
