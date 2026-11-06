package dev.mongocamp.server.service

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationAlternate
import com.mongodb.client.model.CollationCaseFirst
import com.mongodb.client.model.CollationMaxVariable
import com.mongodb.client.model.CollationStrength
import dev.mongocamp.driver.mongodb.mapToBson
import dev.mongocamp.server.exception.ErrorCodes
import dev.mongocamp.server.exception.MongoCampException
import dev.mongocamp.server.model.index.IndexKey
import dev.mongocamp.server.model.index.IndexOptionsRequest
import org.bson.BsonDocument
import org.bson.BsonInt32
import org.bson.BsonString
import org.mongodb.scala.model.IndexOptions
import scala.concurrent.duration.durationToPair
import scala.util.Try

/** Checks and converts the request to create an index. What the database would only answer with an error (or accept as it is: a direction of 5 creates the
  * index `z_5`) is a bad request here.
  */
object IndexCreateService {

  val KeyTypes: List[String] = List("text", "2dsphere", "2d", "hashed")

  private def invalid(message: String) = MongoCampException.badRequestException(message, ErrorCodes.invalidIndexKey)

  def keysDocument(keys: Option[Map[String, Int]], orderedKeys: Option[List[IndexKey]]): BsonDocument = {
    val legacy  = keys.filter(_.nonEmpty)
    val ordered = orderedKeys.filter(_.nonEmpty)
    if (legacy.isDefined && ordered.isDefined) {
      throw invalid("send the keys of the index as `keys` or as `orderedKeys`, not both")
    }
    val document = new BsonDocument()
    legacy.foreach(
      map =>
        map.foreach(
          entry => {
            if (entry._1.isBlank) throw invalid("the name of a field of the index is empty")
            if (entry._2 != 1 && entry._2 != -1) throw invalid(s"the direction of the field ${entry._1} is ${entry._2}, it has to be 1 or -1")
            document.append(entry._1, new BsonInt32(entry._2))
          }
        )
    )
    ordered.foreach(
      list =>
        list.foreach(
          key => {
            if (key.field.isBlank) throw invalid("the name of a field of the index is empty")
            if (document.containsKey(key.field)) throw invalid(s"the field ${key.field} is twice in the keys of the index")
            document.append(key.field, keyValue(key))
          }
        )
    )
    if (document.isEmpty) {
      throw invalid("an index needs at least one key")
    }
    document
  }

  private def keyValue(key: IndexKey) = key.direction match {
    case "1"                        => new BsonInt32(1)
    case "-1"                       => new BsonInt32(-1)
    case other if KeyTypes.contains(other) => new BsonString(other)
    case other                      => throw invalid(s"the direction of the field ${key.field} is `$other`, it has to be 1, -1 or one of ${KeyTypes.mkString(", ")}")
  }

  def collation(map: Map[String, Any]): Collation = {
    def number(key: String, value: Any): Int = value match {
      case n: Number => n.intValue()
      case other     => throw invalid(s"the collation option $key has to be a number, not `$other`")
    }
    def text(key: String, value: Any): String = value match {
      case s: String => s
      case other     => throw invalid(s"the collation option $key has to be a text, not `$other`")
    }
    def flag(key: String, value: Any): Boolean = value match {
      case b: Boolean => b
      case other      => throw invalid(s"the collation option $key has to be true or false, not `$other`")
    }
    def enumeration[T](key: String, value: Any)(read: String => T): T =
      Try(read(text(key, value))).getOrElse(throw invalid(s"the collation option $key does not allow the value `$value`"))

    if (!map.contains("locale")) throw invalid("a collation needs a locale")
    val builder = Collation.builder()
    map.foreach(
      entry =>
        entry._1 match {
          case "locale"          => builder.locale(text("locale", entry._2))
          case "caseLevel"       => builder.caseLevel(flag("caseLevel", entry._2))
          case "caseFirst"       => builder.collationCaseFirst(enumeration("caseFirst", entry._2)(CollationCaseFirst.fromString))
          case "strength"        => builder.collationStrength(Try(CollationStrength.fromInt(number("strength", entry._2))).getOrElse(throw invalid("the collation strength has to be 1 to 5")))
          case "numericOrdering" => builder.numericOrdering(flag("numericOrdering", entry._2))
          case "alternate"       => builder.collationAlternate(enumeration("alternate", entry._2)(CollationAlternate.fromString))
          case "maxVariable"     => builder.collationMaxVariable(enumeration("maxVariable", entry._2)(CollationMaxVariable.fromString))
          case "backwards"       => builder.backwards(flag("backwards", entry._2))
          case "normalization"   => builder.normalization(flag("normalization", entry._2))
          case other             => throw invalid(s"the collation option $other is not known")
        }
    )
    builder.build()
  }

  /** The keys of an index as the database lists it, to create it again: the internal keys of a text index are replaced by its fields. */
  def keysFromIndexDocument(index: BsonDocument): BsonDocument = {
    val weights = Option(index.get("weights")).filter(_.isDocument).map(_.asDocument())
    keysDocument(None, Some(IndexInfoService.indexKeys(index.getDocument("key"), weights)))
  }

  /** The options of an index as the database lists it, to create it again on another collection. */
  def optionsFromIndexDocument(index: BsonDocument): IndexOptions = {
    def flag(name: String) = Option(index.get(name)).exists(_.isBoolean) && index.getBoolean(name).getValue
    var options            = IndexOptions().name(index.getString("name").getValue)
    if (flag("unique")) options = options.unique(true)
    if (flag("sparse")) options = options.sparse(true)
    if (flag("hidden")) options = options.hidden(true)
    Option(index.get("expireAfterSeconds")).filter(_.isNumber).foreach(seconds => options = options.expireAfter(seconds.asNumber().longValue(), java.util.concurrent.TimeUnit.SECONDS))
    Option(index.get("partialFilterExpression")).filter(_.isDocument).foreach(filter => options = options.partialFilterExpression(filter.asDocument()))
    Option(index.get("weights")).filter(_.isDocument).foreach(weights => options = options.weights(weights.asDocument()))
    Option(index.get("default_language")).filter(_.isString).foreach(language => options = options.defaultLanguage(language.asString().getValue))
    Option(index.get("language_override")).filter(_.isString).foreach(field => options = options.languageOverride(field.asString().getValue))
    Option(index.get("textIndexVersion")).filter(_.isNumber).foreach(version => options = options.textVersion(version.asNumber().intValue()))
    Option(index.get("collation")).filter(_.isDocument).foreach(
      collation => {
        // the database adds the version of the collation library to what was asked for
        val map = IndexInfoService.toPlainMap(collation.asDocument()).removed("version")
        options = options.collation(this.collation(map))
      }
    )
    options
  }

  def indexOptions(request: Option[IndexOptionsRequest]): IndexOptions = {
    var indexOptions = IndexOptions()
    request.foreach(
      indexOptionsRequest => {
        indexOptionsRequest.name.foreach(name => indexOptions = indexOptions.name(name))
        indexOptionsRequest.background.foreach(background => indexOptions = indexOptions.background(background))
        indexOptionsRequest.defaultLanguage.foreach(defaultLanguage => indexOptions = indexOptions.defaultLanguage(defaultLanguage))
        indexOptionsRequest.unique.foreach(unique => indexOptions = indexOptions.unique(unique))
        indexOptionsRequest.textVersion.foreach(textVersion => indexOptions = indexOptions.textVersion(textVersion))
        indexOptionsRequest.max.foreach(max => indexOptions = indexOptions.max(max))
        indexOptionsRequest.min.foreach(min => indexOptions = indexOptions.min(min))
        indexOptionsRequest.expireAfter.foreach(
          expireAfter => {
            val expire = scala.concurrent.duration.Duration(expireAfter)
            indexOptions = indexOptions.expireAfter(expire._1, expire._2)
          }
        )
        indexOptionsRequest.sparse.foreach(sparse => indexOptions = indexOptions.sparse(sparse))
        indexOptionsRequest.hidden.foreach(hidden => indexOptions = indexOptions.hidden(hidden))
        indexOptionsRequest.partialFilterExpression.foreach(filter => indexOptions = indexOptions.partialFilterExpression(mapToBson(filter)))
        indexOptionsRequest.weights.foreach(weights => indexOptions = indexOptions.weights(mapToBson(weights)))
        indexOptionsRequest.collation.foreach(map => indexOptions = indexOptions.collation(collation(map)))
      }
    )
    indexOptions
  }
}
