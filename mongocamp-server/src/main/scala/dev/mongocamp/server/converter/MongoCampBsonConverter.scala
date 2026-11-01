package dev.mongocamp.server.converter

import org.bson.types.ObjectId
import org.bson.BsonValue
import org.joda.time.DateTime
import org.mongodb.scala.bson.Document
import org.mongodb.scala.bsonDocumentToDocument
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

  def convertFields(map: Map[String, Any]): Map[String, Any] = {
    val mutableMap = mutable.Map[String, Any]()
    map.foreach(
      element => {
        if (element._1 == "_id") {
          mutableMap.put(element._1, convertIdValue(element._2))
        }
        else {
          element._2 match {
            case value: Map[String, Any] =>
              mutableMap.put(element._1, convertFields(value))
            case value: Iterable[Map[String, Any]] if (value.nonEmpty && value.head.isInstanceOf[Map[String, Any]]) =>
              mutableMap.put(
                element._1,
                value.map(
                  e => convertFields(e)
                )
              )
            case d: DateTime => mutableMap.put(element._1, d.toDate)
            case _ =>
              mutableMap.put(element._1, element._2)
          }
        }
      }
    )
    mutableMap.toMap
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

  def convertIdField(id: Any): ObjectId = {
    id match {
      case s: String   => new ObjectId(s)
      case o: ObjectId => o
      case _           => new ObjectId(id.toString)
    }
  }
}
