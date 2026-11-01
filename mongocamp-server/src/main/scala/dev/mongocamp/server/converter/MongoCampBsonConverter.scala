package dev.mongocamp.server.converter

import dev.mongocamp.server.exception.MongoCampException
import org.bson.types.ObjectId
import org.bson.BsonValue
import org.joda.time.DateTime
import org.mongodb.scala.bson.Document
import org.mongodb.scala.bsonDocumentToDocument
import java.util.Date
import scala.collection.mutable

object MongoCampBsonConverter {
  def documentToMap(document: Document): Map[String, Any] = {
    val map = dev.mongocamp.driver.mongodb.bson.BsonConverter.asMap(document)
    map.map(
      element => {
        element._2 match {
          case objectId: ObjectId =>
            (element._1, objectId.toHexString)
          case _ =>
            (element._1, element._2)
        }
      }
    )
  }

  def documentToMap(document: org.bson.BsonDocument): Map[String, Any] = {
    val map = dev.mongocamp.driver.mongodb.bson.BsonConverter.asMap(document)
    map.map(
      element => {
        element._2 match {
          case objectId: ObjectId =>
            (element._1, objectId.toHexString)
          case _ =>
            (element._1, element._2)
        }
      }
    )
  }

  def convertToOperationMap(conversionMap: Map[String, Any]): Map[String, Any] = {

    val document = mutable.Map[String, Any]()

    convertFields(conversionMap).foreach(
      element => {
        if (element._1.startsWith("$")) {
          document.put(element._1, element._2)
        }
        else {
          if (element._2 == null) {
            addToOperationMap(document, "unset", (element._1, ""))
          }
          else {
            addToOperationMap(document, "set", element)
          }
        }

      }
    )
    document.toMap
  }

  private def addToOperationMap(document: mutable.Map[String, Any], operationType: String, element: (String, Any)): Option[Any] = {
    val setMap = document.getOrElse(
      "$" + operationType, {
        val map = mutable.Map[String, Any]()
        document.put("$" + operationType, map)
        map
      }
    )
    val map: mutable.Map[String, Any] = setMap match {
      case value: mutable.Map[String, Any] =>
        value
      case map: Map[String, Any] =>
        val mutableMap = mutable.Map[String, Any]()
        mutableMap ++ map
        mutableMap
    }
    map.put(element._1, element._2)
  }

  /** Converts the values of a document or filter: dates (`DateTime` to `Date`) and the Extended JSON wrappers `{"$date": ...}` (an ISO-8601 instant, epoch
    * milliseconds or `{"$numberLong": "..."}`) and `{"$numberLong": "..."}`, also nested in objects, lists and the operators of a filter. The wrappers are only
    * read, responses never contain them. The `_id` is converted by [[convertIdValue]].
    */
  def convertFields(map: Map[String, Any]): Map[String, Any] = {
    map.map(
      element =>
        if (element._1 == "_id") element._1 -> convertIdValue(element._2)
        else element._1                     -> convertValue(element._2)
    )
  }

  private def convertValue(value: Any): Any = {
    value match {
      case map: Map[_, _] =>
        val typed = map.asInstanceOf[Map[String, Any]]
        if (typed.size == 1 && typed.contains("$date")) convertDateWrapper(typed("$date"))
        else if (typed.size == 1 && typed.contains("$numberLong")) convertNumberLong(typed("$numberLong"))
        else convertFields(typed)
      case d: DateTime           => d.toDate
      case iterable: Iterable[_] => iterable.map(convertValue).toList
      case other                 => other
    }
  }

  private def convertDateWrapper(value: Any): Date = {
    value match {
      case d: DateTime => d.toDate
      case s: String =>
        IsoInstant
          .parse(s)
          .map(_.toDate)
          .getOrElse(throw MongoCampException.badRequestException(s"""the value of "$$date" must be an ISO-8601 instant with a zone, for example 2024-06-15T12:00:00Z"""))
      case l: Long                  => new Date(l)
      case i: Int                   => new Date(i.toLong)
      case d: Double                => new Date(d.toLong)
      case m: Map[_, _] =>
        val typed = m.asInstanceOf[Map[String, Any]]
        if (typed.size == 1 && typed.contains("$numberLong")) new Date(convertNumberLong(typed("$numberLong")))
        else throw MongoCampException.badRequestException(s"""the value of "$$date" must be an ISO-8601 instant or the epoch milliseconds""")
      case _ =>
        throw MongoCampException.badRequestException(s"""the value of "$$date" must be an ISO-8601 instant or the epoch milliseconds""")
    }
  }

  private def convertNumberLong(value: Any): Long = {
    value match {
      case s: String =>
        s.toLongOption.getOrElse(throw MongoCampException.badRequestException(s"""the value of "$$numberLong" must be a number with up to 64 bit, got $s"""))
      case l: Long => l
      case i: Int  => i.toLong
      case _       => throw MongoCampException.badRequestException(s"""the value of "$$numberLong" must be a string with a number""")
    }
  }

  // operators of a filter on _id, the values of these operators are ids
  private val idOperators = Set("$eq", "$ne", "$gt", "$gte", "$lt", "$lte", "$in", "$nin", "$all")

  /** Converts the value of an `_id` field of a document or filter. The id of a document can be any value, so only the values that are ObjectIds are converted:
    * a string with 24 hex characters or the Extended JSON form `{"$oid": "..."}`, also in the values of the operators `$eq`, `$ne`, `$gt`, `$gte`, `$lt`,
    * `$lte`, `$in`, `$nin` and `$all`. All other values (strings, numbers and operators like `$regex` or `$exists`) are used as they are.
    */
  def convertIdValue(id: Any): Any = {
    id match {
      case objectId: ObjectId                         => objectId
      case string: String if ObjectId.isValid(string) => new ObjectId(string)
      case map: Map[_, _] =>
        val operatorMap = map.asInstanceOf[Map[String, Any]]
        if (operatorMap.size == 1 && operatorMap.contains("$oid")) {
          convertIdField(operatorMap("$oid"))
        }
        else {
          operatorMap.map(
            element => if (idOperators.contains(element._1)) element._1 -> convertIdValue(element._2) else element
          )
        }
      case iterable: Iterable[_] => iterable.map(convertIdValue).toList
      case other                 => other
    }
  }

  private val numberPattern = "-?\\d+(\\.\\d+)?".r

  /** The filter for the id in the path of a request, which is always a string. A document can have an id of any type, so beside the ObjectId (24 hex characters)
    * or the string itself an id that looks like a number is also searched as number: `12345` finds the document with the id `"12345"` and the one with the
    * id `12345`. MongoDB compares numbers of different types by value.
    */
  def pathIdFilter(id: String): Map[String, Any] = {
    val candidates: List[Any] = convertIdValue(id) match {
      case objectId: ObjectId => List(objectId)
      case _ if numberPattern.matches(id) =>
        val number: Any = id.toLongOption.getOrElse(id.toDouble)
        List(id, number)
      case _ => List(id)
    }
    if (candidates.size == 1) Map("_id" -> candidates.head) else Map("_id" -> Map("$in" -> candidates))
  }

  /** The id of a document as string for the responses: the hex string of an ObjectId, otherwise the value itself. */
  def idToString(id: Any): String = {
    id match {
      case bson: BsonValue if bson.isObjectId => bson.asObjectId().getValue.toHexString
      case bson: BsonValue if bson.isString   => bson.asString().getValue
      case bson: BsonValue if bson.isInt32    => bson.asInt32().getValue.toString
      case bson: BsonValue if bson.isInt64    => bson.asInt64().getValue.toString
      case bson: BsonValue if bson.isNumber   => bson.asNumber().doubleValue().toString
      case objectId: ObjectId                 => objectId.toHexString
      case other                              => other.toString
    }
  }

  // only for ids that must be an ObjectId (file ids), the ids of documents can have any type (see convertIdValue)
  def convertIdField(id: Any): ObjectId = {
    id match {
      case o: ObjectId => o
      case other =>
        val value = other.toString
        if (ObjectId.isValid(value)) new ObjectId(value)
        else throw MongoCampException.badRequestException("the id is not a valid ObjectId, it must be a string with 24 hexadecimal characters")
    }
  }
}
